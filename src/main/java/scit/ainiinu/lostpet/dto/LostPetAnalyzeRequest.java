package scit.ainiinu.lostpet.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "lostPetId는 필수입니다.")
    private Long lostPetId;

    private String image;

    private String imageUrl;

    private String mode;

    private String queryText;

    private Double latitude;
    private Double longitude;

    @AssertTrue(message = "image 또는 imageUrl 중 하나는 필수입니다.")
    public boolean isImageProvided() {
        return hasText(image) || hasText(imageUrl);
    }

    public String resolveImageSource() {
        if (hasText(image)) {
            return image;
        }
        return imageUrl;
    }

    public String resolveMode() {
        if (hasText(mode)) {
            return mode.trim();
        }
        return "LOST";
    }

    public LostPetAnalyzeRequest normalizeForAi() {
        String resolvedImage = resolveImageSource();
        return LostPetAnalyzeRequest.builder()
                .lostPetId(lostPetId)
                .image(resolvedImage)
                .imageUrl(resolvedImage)
                .mode(resolveMode())
                .queryText(queryText)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
