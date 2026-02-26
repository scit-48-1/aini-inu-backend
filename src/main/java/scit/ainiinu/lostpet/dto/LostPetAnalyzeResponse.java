package scit.ainiinu.lostpet.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record LostPetAnalyzeResponse(
        String summary,
        boolean fallback,
        List<LostPetAnalyzeCandidateResponse> candidates
) {
}
