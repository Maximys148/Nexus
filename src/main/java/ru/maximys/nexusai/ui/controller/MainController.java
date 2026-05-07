package ru.maximys.nexusai.ui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ru.maximys.nexusai.MainApplication;
import ru.maximys.nexusai.backend.service.update.UpdateService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private UpdateService updateService;

    @FXML
    private Label versionLabel;

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
    private Button updateButton;

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
        // Устанавливаем локальную версию из pom.xml
        versionLabel.setText("Локальная: v" + updateService.getCurrentVersion());

        new Thread(() -> {
            try {
                if (updateService.isUpdateAvailable()) {
                    // Получаем инфо о релизе, чтобы узнать ТОЧНУЮ версию на GitHub
                    Map<String, Object> latest = updateService.getLatestReleaseInfo();
                    String latestVer = (String) latest.get("tag_name");

                    Platform.runLater(() -> {
                        updateButton.setVisible(true);
                        updateButton.setManaged(true);
                        // Обновляем текст, чтобы пользователь видел, на что он обновляется
                        versionLabel.setText("v" + updateService.getCurrentVersion() + " -> " + latestVer);
                    });
                }
            } catch (Exception e) {
                log.error("Ошибка проверки версий", e);
            }
        }).start();

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
    private void onUpdateClick() {
        new Thread(() -> {
            try {
                // Те же пути, что и раньше
                String tempJarPath = Paths.get(System.getProperty("java.io.tmpdir"), "update.jar").toString();
                Map<String, Object> latest = updateService.getLatestReleaseInfo();
                List<Map<String, Object>> assets = (List<Map<String, Object>>) latest.get("assets");
                String downloadUrl = (String) assets.get(0).get("browser_download_url");
                updateService.downloadNewVersion(downloadUrl, tempJarPath);

                String currentJarPath = new File(MainApplication.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI()).getPath();
                File exeFile = new File(currentJarPath).getParentFile().getParentFile();
                String exePath = new File(exeFile, "NexusAI.exe").getAbsolutePath();

                // ФОРМИРУЕМ КОМАНДЫ ПОСТРОЧНО (Безопасно)
                List<String> commands = Arrays.asList(
                        "@echo off",
                        "chcp 65001 > nul",
                        "echo [Nexus AI Update] Ожидание закрытия программы...",
                        "timeout /t 3 /nobreak > nul",
                        "echo [Nexus AI Update] Замена файлов...",
                        "move /y \"" + tempJarPath + "\" \"" + currentJarPath + "\"",
                        "echo [Nexus AI Update] Перезапуск...",
                        "start \"\" \"" + exePath + "\"",
                        "del \"%~f0\""
                );

                // Записываем файл в кодировке UTF-8
                Files.write(Paths.get("updater.bat"), commands, StandardCharsets.UTF_8);

                // Запуск
                new ProcessBuilder("cmd", "/c", "start", "updater.bat").start();
                System.exit(0);

            } catch (Exception e) {
                log.error("Критическая ошибка обновления: ", e);
            }
        }).start();
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