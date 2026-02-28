package scit.ainiinu.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PresignedImageRequest {

    @NotBlank
    private String purpose;

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;
}
