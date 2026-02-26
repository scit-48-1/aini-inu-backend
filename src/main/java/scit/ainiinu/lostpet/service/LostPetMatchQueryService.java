package scit.ainiinu.lostpet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.lostpet.dto.LostPetMatchCandidateResponse;
import scit.ainiinu.lostpet.repository.LostPetMatchRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostPetMatchQueryService {

    private final LostPetMatchRepository lostPetMatchRepository;

    public Slice<LostPetMatchCandidateResponse> findCandidates(Long lostPetId, Pageable pageable) {
        return lostPetMatchRepository.findByLostPetReportId(lostPetId, pageable)
                .map(match -> LostPetMatchCandidateResponse.builder()
                        .sightingId(match.getSighting().getId())
                        .finderId(match.getSighting().getFinderId())
                        .similarityTotal(match.getSimilarityTotal())
                        .status(match.getStatus().name())
                        .build());
    }
}
