package ru.maximys.nexusai.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Component
@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @FXML
    private HBox titleBar;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button aboutButton;

    @FXML
    private Button exitButton;

    // Переменные для перетаскивания окна
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML private StackPane contentArea;

    // Внедряем контекст Spring, чтобы он мог создавать контроллеры для новых окон
    private final ApplicationContext springContext;

    public MainController(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    // Универсальный метод для смены экрана
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/maximys/nexusai/" + fxmlPath));
            // ВАЖНО: используем Spring для создания контроллера загружаемого файла
            loader.setControllerFactory(springContext::getBean);

            Parent view = loader.load();

            contentArea.getChildren().clear(); // Очищаем старый экран
            contentArea.getChildren().add(view); // Добавляем новый
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onChatButtonClick() {
        log.info("Переход в окно с чатом");
        loadView("chat-view.fxml");
    }

    @FXML
    public void onSettingButtonClick() {
        log.info("Переход в окно с настройками");
        loadView("setting-view.fxml");
    }

    @FXML
    public void onImageButtonClick() {
        // Здесь можно будет загрузить другой файл, например image-view.fxml
        log.info("Переход к генерации изображений");
    }

    @FXML
    public void initialize() {
        log.info("Приложение запущено");

        // Делаем заголовок перетаскиваемым
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    protected void onSearchButtonClick() {
        // Здесь будет переход к окну поиска
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Поиск");
        alert.setHeaderText(null);
        alert.setContentText("Здесь будет окно поиска");
        alert.showAndWait();
    }

    @FXML
    protected void onHistoryButtonClick() {
        // Здесь будет переход к истории поиска
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("История");
        alert.setHeaderText(null);
        alert.setContentText("Здесь будет история запросов");
        alert.showAndWait();
    }

    @FXML
    protected void onSettingsButtonClick() {
        // Здесь будет переход к настройкам
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Настройки");
        alert.setHeaderText(null);
        alert.setContentText("Здесь будут настройки приложения");
        alert.showAndWait();
    }

    @FXML
    protected void onAboutButtonClick() {
        // Показываем информацию о программе
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("AiSearchFX");
        alert.setContentText("Версия 1.0\n\nПриложение для интеллектуального поиска\nс использованием AI");
        alert.showAndWait();
    }

    @FXML
    protected void onExitButtonClick() {
        // Закрываем приложение
        Platform.exit();
    }

    @FXML
    protected void onMinimizeClick() {
        // Сворачиваем окно
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }
    @FXML
    protected void onMaximizeClick() {
        // Переключаем между развернутым и оконным режимом
        Stage stage = (Stage) maximizeButton.getScene().getWindow();

        if (stage.isMaximized()) {
            // Если уже развернуто — возвращаем в оконный режим
            stage.setMaximized(false);
            maximizeButton.setText("🗖");  // значок развернуть
        } else {
            // Разворачиваем на весь экран
            stage.setMaximized(true);
            maximizeButton.setText("❐");  // значок восстановить (два квадрата)
        }
    }

    @FXML
    protected void onCloseClick() {
        // Закрываем приложение
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}