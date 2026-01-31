package kr.hirekit.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient publicDataWebClient() {
        return WebClient.builder()
                .baseUrl("https://apis.data.go.kr")
                .build();
    }
}
