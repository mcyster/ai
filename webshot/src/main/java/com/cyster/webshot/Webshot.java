package com.cyster.webshot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class Webshot {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 1) {
            System.out.println("Usage: java Webshot <URL>");
            System.exit(1);
        }

        String url = args[0];

        String chromeDriver = System.getenv("CHROMEDRIVER");
        if (chromeDriver == null || chromeDriver.isEmpty()) {
            throw new IllegalStateException("CHROMEDRIVER environment variable is not set");
        }
        System.setProperty("webdriver.chrome.driver", chromeDriver);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-debugging-port=9222");

        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

        Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
                .takeScreenshot(driver);

        File file = getUniqueFile(url, ".png");
        try {
            ImageIO.write(screenshot.getImage(), "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        driver.quit();

        System.out.println(file.getAbsolutePath());
    }

    static File getUniqueFile(String url, String extension) throws URISyntaxException {
        String baseName = urlToFileName(url) + extension;
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, baseName);
        int count = 1;

        while (file.exists()) {
            String newName = baseName + "-" + count;
            file = new File(tempDir, newName);
            count++;
        }
        return file;
    }

    private static String urlToFileName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            throw new URISyntaxException(url, "Invalid URL");
        }

        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        String path = uri.getPath().replaceAll("^/+", "").replace("/", "-");
        String fileName = domain + (path.isEmpty() ? "" : "-" + path);
        fileName = fileName.replaceAll("[^a-zA-Z0-9_-]", "-");

        return fileName;
    }

}
