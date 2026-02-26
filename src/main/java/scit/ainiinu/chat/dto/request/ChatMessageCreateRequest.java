package scit.ainiinu.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String content;

    private String messageType;

    private String clientMessageId;
}
