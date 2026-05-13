package ru.maximys.nexus.backend.service;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.maximys.nexus.backend.config.AppSettings;
import ru.maximys.nexus.backend.service.update.UpdateService;

import java.util.Map;

// Сервис отвечающий за бизнес логику контролера MainController

@Service
public class MainService {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);

    private final LanguageService langService;
    private final UpdateService updateService;
    private final NavigationService navService;
    private final SettingService settingService;
    private final AppSettings appSettings;

    // Переменные строго для ПЕРЕТАСКИВАНИЯ окна
    private double dragStartX = 0;
    private double dragStartY = 0;

    // Переменные строго для ИЗМЕНЕНИЯ РАЗМЕРА окна
    private double resizeStartX = 0;
    private double resizeStartY = 0;
    private boolean resizeLeft = false;
    private boolean resizeRight = false;
    private boolean resizeTop = false;
    private boolean resizeBottom = false;
    private static final int RESIZE_MARGIN = 6;

    public MainService(LanguageService langService, UpdateService updateService, NavigationService navService, SettingService settingService, AppSettings appSettings) {
        this.langService = langService;
        this.updateService = updateService;
        this.navService = navService;
        this.settingService = settingService;
        this.appSettings = appSettings;
    }

    public void initMainView(HBox titleBar, Map<String, Labeled> uiElements) {
        // 1. Настраиваем геометрию(изменение размера окна) и перетаскивание
        manageWindowGeometry(titleBar);

        // 2. Локализация
        bindUserInterface(uiElements);

        // 3. Масштабирование
        Platform.runLater(() -> {
            settingService.applyScaling(appSettings.getSavedScale(), titleBar);
        });

        // 4. Обновления
        Labeled versionLabel = uiElements.get("versionLabel");
        versionLabel.setText("v" + updateService.getCurrentVersion());
        Labeled updateButton = uiElements.get("updateButton");
        updateService.checkUpdates(versionInfo -> {
            updateButton.setVisible(true);
            updateButton.setManaged(true);
            versionLabel.setText(versionInfo);
        });
    }

    public void manageWindowGeometry(HBox titleBar) {
        // Подключаем перетаскивание с уникальными переменными координат
        makeWindowDraggable(titleBar);

        // Безопасное ожидание прикрепления к Scene и Stage
        titleBar.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow instanceof Stage stage) {
                        setupStageGeometry(stage, newScene);
                    }
                });
            }
        });
    }

    private void setupStageGeometry(Stage stage, Scene scene) {
        stage.setMinWidth(450);
        stage.setMinHeight(350);

        stage.setWidth(appSettings.getWindowWidth());
        stage.setHeight(appSettings.getWindowHeight());

        scene.addEventHandler(MouseEvent.MOUSE_MOVED, (MouseEvent event) -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();

            resizeLeft = x < RESIZE_MARGIN;
            resizeRight = x > width - RESIZE_MARGIN;
            resizeTop = y < RESIZE_MARGIN;
            resizeBottom = y > height - RESIZE_MARGIN;

            if (resizeLeft && resizeTop) scene.setCursor(Cursor.NW_RESIZE);
            else if (resizeLeft && resizeBottom) scene.setCursor(Cursor.SW_RESIZE);
            else if (resizeRight && resizeTop) scene.setCursor(Cursor.NE_RESIZE);
            else if (resizeRight && resizeBottom) scene.setCursor(Cursor.SE_RESIZE);
            else if (resizeLeft) scene.setCursor(Cursor.W_RESIZE);
            else if (resizeRight) scene.setCursor(Cursor.E_RESIZE);
            else if (resizeTop) scene.setCursor(Cursor.N_RESIZE);
            else if (resizeBottom) scene.setCursor(Cursor.S_RESIZE);
            else scene.setCursor(Cursor.DEFAULT);
        });

        // Изменение размера использует свои изолированные переменные resizeStartX/Y
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            resizeStartX = stage.getX() - event.getScreenX();
            resizeStartY = stage.getY() - event.getScreenY();
        });

        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            if (scene.getCursor() == Cursor.DEFAULT) return;

            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();

            if (resizeRight) {
                double newWidth = mouseX - stage.getX();
                if (newWidth >= stage.getMinWidth()) stage.setWidth(newWidth);
            } else if (resizeLeft) {
                double oldWidth = stage.getWidth();
                double newWidth = stage.getX() + oldWidth - mouseX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setX(mouseX);
                    stage.setWidth(newWidth);
                }
            }

            if (resizeBottom) {
                double newHeight = mouseY - stage.getY();
                if (newHeight >= stage.getMinHeight()) stage.setHeight(newHeight);
            } else if (resizeTop) {
                double oldHeight = stage.getHeight();
                double newHeight = stage.getY() + oldHeight - mouseY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setY(mouseY);
                    stage.setHeight(newHeight);
                }
            }
        });

        stage.showingProperty().addListener((observable, oldValue, isShowing) -> {
            if (!isShowing) { // Сработает при любом закрытии/скрытии окна
                appSettings.setWindowWidth(stage.getWidth());
                appSettings.setWindowHeight(stage.getHeight());
                appSettings.flush();
            }
        });

        stage.setOnCloseRequest((WindowEvent event) -> {
            appSettings.setWindowWidth(stage.getWidth());
            appSettings.setWindowHeight(stage.getHeight());
            appSettings.flush();
        });
    }

    private void makeWindowDraggable(HBox titleBar) {
        // Перетаскивание использует свои изолированные переменные dragStartX/Y
        titleBar.setOnMousePressed((MouseEvent event) -> {
            dragStartX = event.getScreenX();
            dragStartY = event.getScreenY();
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            // Если курсор изменен на стрелки ресайза, не разрешаем перетаскивание
            if (titleBar.getScene().getCursor() != Cursor.DEFAULT) return;

            Stage stage = (Stage) titleBar.getScene().getWindow();

            double deltaX = event.getScreenX() - dragStartX;
            double deltaY = event.getScreenY() - dragStartY;

            stage.setX(stage.getX() + deltaX);
            stage.setY(stage.getY() + deltaY);

            dragStartX = event.getScreenX();
            dragStartY = event.getScreenY();
        });
    }


    // Управляет всеми ключами переводов
    private void bindUserInterface(Map<String, Labeled> elements) {
        langService.bindText(elements.get("functions"), "ui.sidebar.functions");
        langService.bindText(elements.get("profile"), "ui.titlebar.profile");
        langService.bindText(elements.get("welcome"), "ui.main.welcome");
        langService.bindText(elements.get("textButton"), "ui.sidebar.text");
        langService.bindText(elements.get("imageButton"), "ui.sidebar.image");
        langService.bindText(elements.get("videoButton"), "ui.sidebar.video");
        langService.bindText(elements.get("audioButton"), "ui.sidebar.audio");
        langService.bindText(elements.get("settingsButton"), "ui.sidebar.settings");
        langService.bindText(elements.get("updateButton"), "ui.sidebar.update");
    }

    // --- МЕТОДЫ ПЕРЕХОДА МЕЖДУ ЭКРАНАМИ ---

    public void navigateToChat(StackPane area) {
        log.info("Переход в раздел текст");
        navService.load(area, "chat-view.fxml");
    }

    public void navigateToSettings(StackPane area) {
        log.info("Переход в раздел настроек");
        navService.load(area, "setting-view.fxml");
    }

    public void navigateToImage(StackPane area) {
        log.info("Переход в раздел изображения");
        navService.load(area, "image-view.fxml");
    }

    public void navigateToAudio(StackPane area) {
        log.info("Переход в раздел аудио");
        navService.load(area, "audio-view.fxml");
    }

    public void handleUpdateAction() {
        updateService.processUpdate();
    }

    // --- УПРАВЛЕНИЕ СОСТОЯНИЕМ ОКНА ---
    public void minimize(Button btn) {
        getStage(btn).setIconified(true);
    }

    public void maximize(Button btn) {
        Stage stage = getStage(btn);
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            btn.setText("🗖");
        } else {
            stage.setMaximized(true);
            btn.setText("❐");
        }
    }

    public void exit(Node node) {
        if (node != null && node.getScene() != null && node.getScene().getWindow() instanceof Stage stage) {
            // Запоминаем размеры ровно в момент клика по кнопке "Закрыть"
            appSettings.setWindowWidth(stage.getWidth());
            appSettings.setWindowHeight(stage.getHeight());
            appSettings.flush(); // Мгновенный пуш в Preferences
        }
        Platform.exit();
        System.exit(0);
    }

    private Stage getStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }
}
