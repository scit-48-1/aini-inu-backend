package scit.ainiinu.lostpet.integration.ai;

import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;

public interface LostPetAiClient {
    LostPetAiResult analyze(LostPetAnalyzeRequest request);
}
