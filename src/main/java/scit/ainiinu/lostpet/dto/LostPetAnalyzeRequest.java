package scit.ainiinu.lostpet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostPetAnalyzeRequest {

    @NotBlank
    private String imageUrl;

    @NotBlank
    private String mode;

    private Double latitude;
    private Double longitude;
}
