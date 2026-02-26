package scit.ainiinu.lostpet.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeCandidateResponse;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeResponse;
import scit.ainiinu.lostpet.integration.ai.LostPetAiCandidate;
import scit.ainiinu.lostpet.integration.ai.LostPetAiClient;
import scit.ainiinu.lostpet.integration.ai.LostPetAiResult;

@Service
@RequiredArgsConstructor
public class LostPetAnalyzeService {

    private final LostPetAiClient lostPetAiClient;

    @Transactional(readOnly = true)
    public LostPetAnalyzeResponse analyze(LostPetAnalyzeRequest request) {
        try {
            LostPetAiResult result = lostPetAiClient.analyze(request);
            if (result == null) {
                return fallbackResponse();
            }
            List<LostPetAiCandidate> aiCandidates = result.candidates() == null ? List.of() : result.candidates();
            List<LostPetAnalyzeCandidateResponse> candidates = aiCandidates.stream()
                    .map(this::toCandidateResponse)
                    .toList();
            return LostPetAnalyzeResponse.builder()
                    .summary(result.summary() == null ? "" : result.summary())
                    .fallback(false)
                    .candidates(candidates)
                    .build();
        } catch (Exception exception) {
            return fallbackResponse();
        }
    }

    private LostPetAnalyzeResponse fallbackResponse() {
        return LostPetAnalyzeResponse.builder()
                .summary("manual_ready")
                .fallback(true)
                .candidates(List.of())
                .build();
    }

    private LostPetAnalyzeCandidateResponse toCandidateResponse(LostPetAiCandidate candidate) {
        return LostPetAnalyzeCandidateResponse.builder()
                .sightingId(candidate.sightingId())
                .finderId(candidate.finderId())
                .similarityTotal(candidate.similarityTotal())
                .build();
    }
}
