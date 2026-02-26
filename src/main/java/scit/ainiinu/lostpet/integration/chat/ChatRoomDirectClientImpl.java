package scit.ainiinu.lostpet.integration.chat;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChatRoomDirectClientImpl implements ChatRoomDirectClient {

    private final RestTemplate restTemplate;

    @Value("${lostpet.chat.base-url}")
    private String chatBaseUrl;

    @Value("${lostpet.chat.direct-create-path}")
    private String directCreatePath;

    public ChatRoomDirectClientImpl(@Qualifier("chatRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Long createDirectRoom(Long partnerId) {
        String endpoint = chatBaseUrl + directCreatePath;
        Map<String, Object> response = restTemplate.postForObject(
                endpoint,
                Map.of("partnerId", partnerId),
                Map.class
        );
        if (response == null) {
            return null;
        }
        Object dataObj = response.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object roomIdObj = dataMap.get("chatRoomId");
            if (roomIdObj instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }
}
