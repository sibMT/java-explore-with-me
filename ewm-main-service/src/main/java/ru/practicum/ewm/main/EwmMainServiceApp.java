package ru.practicum.ewm.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.practicum.ewm")
public class EwmMainServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(EwmMainServiceApp.class, args);
    }
}
