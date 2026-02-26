package scit.ainiinu.lostpet.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record LostPetAnalyzeCandidateResponse(
        Long sightingId,
        Long finderId,
        BigDecimal similarityTotal
) {
}
