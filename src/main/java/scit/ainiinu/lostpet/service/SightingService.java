package scit.ainiinu.lostpet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.lostpet.domain.Sighting;
import scit.ainiinu.lostpet.dto.SightingCreateRequest;
import scit.ainiinu.lostpet.dto.SightingResponse;
import scit.ainiinu.lostpet.repository.SightingRepository;

@Service
@RequiredArgsConstructor
public class SightingService {

    private final SightingRepository sightingRepository;

    @Transactional
    public SightingResponse create(Long memberId, SightingCreateRequest request) {
        Sighting saved = sightingRepository.save(Sighting.create(
                memberId,
                request.getPhotoUrl(),
                request.getFoundAt(),
                request.getFoundLocation(),
                request.getMemo()
        ));
        return SightingResponse.builder()
                .sightingId(saved.getId())
                .status(saved.getStatus().name())
                .foundAt(saved.getFoundAt())
                .build();
    }
}
