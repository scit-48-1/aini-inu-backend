package scit.ainiinu.lostpet.integration.chat;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
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
    public Long createDirectRoom(Long partnerId, String authorizationHeader) {
        String endpoint = chatBaseUrl + directCreatePath;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (hasText(authorizationHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(Map.of("partnerId", partnerId), headers);

        ResponseEntity<Map<String, Object>> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            throw new ChatDirectClientException(
                    ChatDirectFailureType.AUTH,
                    "chat direct authorization failed",
                    exception
            );
        } catch (HttpStatusCodeException | ResourceAccessException exception) {
            throw new ChatDirectClientException(
                    ChatDirectFailureType.CONNECT,
                    "chat direct connection failed",
                    exception
            );
        } catch (Exception exception) {
            throw new ChatDirectClientException(
                    ChatDirectFailureType.UNKNOWN,
                    "chat direct unknown failure",
                    exception
            );
        }

        Map<String, Object> response = responseEntity.getBody();
        if (response == null) {
            throw new ChatDirectClientException(ChatDirectFailureType.RESPONSE_SCHEMA, "chat direct response body is null");
        }

        Object dataObj = response.get("data");
        if (dataObj instanceof Map<?, ?> dataMap) {
            Object roomIdObj = dataMap.get("chatRoomId");
            if (roomIdObj instanceof Number number) {
                return number.longValue();
            }
        }
        throw new ChatDirectClientException(ChatDirectFailureType.RESPONSE_SCHEMA, "chat direct response schema invalid");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
