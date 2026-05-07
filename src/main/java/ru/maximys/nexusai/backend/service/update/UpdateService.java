package ru.maximys.nexusai.backend.service.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.maximys.nexusai.ui.controller.MainController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateService.class);

    @Value("${app.version}")
    private String currentVersion;

    // ВАЖНО: Используем api.github.com вместо github.com
    private final String GITHUB_API_URL = "https://api.github.com/repos/Maximys148/NexusAI/releases/latest";

    public String getCurrentVersion() {
        return currentVersion;
    }

    public Map<String, Object> getLatestReleaseInfo() {
        log.info("Запрос данных о релизе по адресу: {}", GITHUB_API_URL);
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(GITHUB_API_URL, Map.class);
            if (response == null) {
                log.warn("Ответ от GitHub API пуст");
            }
            return response;
        } catch (Exception e) {
            log.error("Ошибка при обращении к GitHub API: {}", e.getMessage());
            return null;
        }
    }

    public boolean isUpdateAvailable() {
        Map<String, Object> latest = getLatestReleaseInfo();
        if (latest == null) {
            log.warn("Не удалось получить информацию о последнем релизе");
            return false;
        }

        String latestTagName = (String) latest.get("tag_name");
        if (latestTagName == null) {
            log.warn("В ответе API отсутствует поле 'tag_name'");
            return false;
        }

        // Очистка и сравнение
        String latestVer = latestTagName.replaceAll("[^0-9.]", "");
        String currentVer = currentVersion.replaceAll("[^0-9.]", "");

        log.info("Сравнение версий: Локальная [{}], На GitHub [{}]", currentVer, latestVer);

        boolean available = !currentVer.equals(latestVer);
        log.info("Обновление доступно: {}", available);

        return available;
    }

    public void downloadNewVersion(String downloadUrl, String tempPath) throws IOException {
        log.info("Начинаем стабильное скачивание: {}", downloadUrl);

        java.net.URL url = new java.net.URL(downloadUrl);
        try (java.io.InputStream in = url.openStream()) {
            java.nio.file.Files.copy(in,
                    java.nio.file.Paths.get(tempPath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Файл успешно сохранен в: {}", tempPath);
        } catch (Exception e) {
            log.error("Ошибка при копировании файла: {}", e.getMessage());
            throw e;
        }
    }
}
