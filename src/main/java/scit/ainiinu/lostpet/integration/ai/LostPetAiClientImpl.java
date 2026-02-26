package scit.ainiinu.lostpet.integration.ai;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;

@Component
public class LostPetAiClientImpl implements LostPetAiClient {

    private final RestTemplate restTemplate;

    @Value("${lostpet.ai.base-url}")
    private String baseUrl;

    @Value("${lostpet.ai.analyze-path}")
    private String analyzePath;

    public LostPetAiClientImpl(@Qualifier("lostPetAiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public LostPetAiResult analyze(LostPetAnalyzeRequest request) {
        String endpoint = baseUrl + analyzePath;
        ResponseEntity<LostPetAiResult> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {
                }
        );
        LostPetAiResult body = response.getBody();
        if (body == null) {
            return new LostPetAiResult("empty", List.of());
        }
        return body;
    }
}
