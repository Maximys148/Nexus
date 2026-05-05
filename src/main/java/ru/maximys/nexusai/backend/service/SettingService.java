package ru.maximys.nexusai.backend.service;

import atlantafx.base.theme.*;
import javafx.application.Application;
import org.springframework.stereotype.Service;

@Service
public class SettingService {

    public void apply(String themeName) {

        if (themeName == null) return;

        switch (themeName){
            case "Primer Light" -> Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            case "Primer Dark" -> Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            case "Nord Light" -> Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            case "Nord Dark" -> Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
            case "Cupertino Light" -> Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            case "Cupertino Dark" -> Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
            case "Dracula" -> Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        }
    }
}
