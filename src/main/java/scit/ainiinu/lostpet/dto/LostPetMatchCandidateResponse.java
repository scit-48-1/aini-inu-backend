package scit.ainiinu.lostpet.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record LostPetMatchCandidateResponse(
        Long sightingId,
        Long finderId,
        BigDecimal similarityTotal,
        String status
) {
}
