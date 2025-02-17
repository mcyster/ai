package com.cyster.webshot;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class Webshot {
    public static void main(String[] args) {
        // Set the path to your WebDriver executable
        System.setProperty("webdriver.chrome.driver",
                "/nix/store/7g7v07wcac6shkdjik6j3sj7kcazffr3-chromedriver-unwrapped-133.0.6943.98/bin/chromedriver");

        WebDriver driver = new ChromeDriver();
        driver.get("https://example.com");

        Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000))
                .takeScreenshot(driver);

        try {
            ImageIO.write(screenshot.getImage(), "PNG", new File("screenshot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        driver.quit();
    }
}
