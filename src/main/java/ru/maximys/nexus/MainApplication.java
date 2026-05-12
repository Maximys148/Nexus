package ru.maximys.nexus;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import ru.maximys.nexus.backend.service.MainApplicationService;

// Класс запуска приложения

@SpringBootApplication
public class MainApplication extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(MainApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainApplicationService mainApplicationService = springContext.getBean(MainApplicationService.class);
        mainApplicationService.showMainScene(stage);
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    public static ConfigurableApplicationContext getContext() {
        return springContext;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
