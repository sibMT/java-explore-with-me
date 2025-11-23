package ru.practicum.ewm.main.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Value("${stats.base-url}")
    private String baseUrl;

    @Bean
    public StatsClient statsClient() {
        return new StatsClient(baseUrl);
    }
}
