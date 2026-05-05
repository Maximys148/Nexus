package ru.maximys.nexusai;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.maximys.nexusai.backend.config.AppSettings;
import ru.maximys.nexusai.backend.service.SettingService;

import java.util.Objects;

@SpringBootApplication()
public class MainApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Запускаем Spring при старте приложения
        springContext = new SpringApplicationBuilder(MainApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        String savedTheme = springContext.getBean(AppSettings.class).getSavedTheme();
        springContext.getBean(SettingService.class).apply(savedTheme);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/maximys/nexusai/main-view.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 600);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);

        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/icons/icon.png"))));
        stage.setTitle("Nexus AI");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Закрываем контекст Spring при выходе
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
