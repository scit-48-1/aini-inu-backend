package scit.ainiinu.lostpet.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LostPetClientConfig {

    @Bean(name = "lostPetAiRestTemplate")
    public RestTemplate lostPetAiRestTemplate(
            RestTemplateBuilder builder,
            @Value("${lostpet.ai.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${lostpet.ai.read-timeout-ms:4000}") int readTimeoutMs
    ) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }

    @Bean(name = "chatRestTemplate")
    public RestTemplate chatRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
