package scit.ainiinu.lostpet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.lostpet.domain.LostPetMatch;
import scit.ainiinu.lostpet.domain.LostPetMatchStatus;
import scit.ainiinu.lostpet.domain.LostPetReport;
import scit.ainiinu.lostpet.domain.LostPetReportStatus;
import scit.ainiinu.lostpet.domain.Sighting;
import scit.ainiinu.lostpet.domain.SightingStatus;
import scit.ainiinu.lostpet.dto.LostPetMatchApproveRequest;
import scit.ainiinu.lostpet.dto.LostPetMatchResponse;
import scit.ainiinu.lostpet.error.LostPetErrorCode;
import scit.ainiinu.lostpet.error.LostPetException;
import scit.ainiinu.lostpet.integration.chat.ChatRoomDirectClient;
import scit.ainiinu.lostpet.repository.LostPetMatchRepository;
import scit.ainiinu.lostpet.repository.LostPetReportRepository;
import scit.ainiinu.lostpet.repository.SightingRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class LostPetMatchApprovalService {

    private final LostPetReportRepository lostPetReportRepository;
    private final SightingRepository sightingRepository;
    private final LostPetMatchRepository lostPetMatchRepository;
    private final ChatRoomDirectClient chatRoomDirectClient;

    @Transactional
    public LostPetMatchResponse approve(Long lostPetId, Long memberId, LostPetMatchApproveRequest request) {
        long startedAt = System.currentTimeMillis();
        LostPetReport report = lostPetReportRepository.findById(lostPetId)
                .orElseThrow(() -> new LostPetException(LostPetErrorCode.L404_NOT_FOUND));
        if (!report.getOwnerId().equals(memberId)) {
            throw new LostPetException(LostPetErrorCode.L403_FORBIDDEN);
        }
        if (report.getStatus() != LostPetReportStatus.ACTIVE) {
            throw new LostPetException(LostPetErrorCode.L410_REPORT_RESOLVED);
        }

        Long sightingId = request.getSightingId();
        Sighting sighting = sightingRepository.findById(sightingId)
                .orElseThrow(() -> new LostPetException(LostPetErrorCode.L404_NOT_FOUND));
        if (sighting.getStatus() == SightingStatus.CLOSED) {
            throw new LostPetException(LostPetErrorCode.L409_MATCH_CONFLICT);
        }

        LostPetMatch match = lostPetMatchRepository.findByLostPetReportIdAndSightingId(lostPetId, sightingId)
                .orElseThrow(() -> new LostPetException(LostPetErrorCode.L404_NOT_FOUND));

        LostPetMatchStatus status = match.getStatus();
        if (status == LostPetMatchStatus.INVALIDATED || status == LostPetMatchStatus.REJECTED) {
            throw new LostPetException(LostPetErrorCode.L409_MATCH_CONFLICT);
        }
        if (status == LostPetMatchStatus.CHAT_LINKED) {
            return toResponse(match);
        }
        if (status == LostPetMatchStatus.PENDING_APPROVAL) {
            match.approve(memberId);
        }

        try {
            Long chatRoomId = chatRoomDirectClient.createDirectRoom(sighting.getFinderId());
            if (chatRoomId == null) {
                match.markPendingChatLink();
                log.warn(
                        "lostpet.match.approve pending-chat-link lostPetId={} sightingId={} memberId={} elapsedMs={}",
                        lostPetId,
                        sightingId,
                        memberId,
                        System.currentTimeMillis() - startedAt
                );
            } else {
                match.linkChatRoom(chatRoomId);
                log.info(
                        "lostpet.match.approve chat-linked lostPetId={} sightingId={} memberId={} chatRoomId={} elapsedMs={}",
                        lostPetId,
                        sightingId,
                        memberId,
                        chatRoomId,
                        System.currentTimeMillis() - startedAt
                );
            }
        } catch (Exception exception) {
            match.markPendingChatLink();
            log.warn(
                    "lostpet.match.approve chat-create-failed lostPetId={} sightingId={} memberId={} elapsedMs={} reason={}",
                    lostPetId,
                    sightingId,
                    memberId,
                    System.currentTimeMillis() - startedAt,
                    exception.getClass().getSimpleName()
            );
        }

        LostPetMatch saved = lostPetMatchRepository.save(match);
        return toResponse(saved);
    }

    private LostPetMatchResponse toResponse(LostPetMatch match) {
        return LostPetMatchResponse.builder()
                .matchId(match.getId())
                .status(match.getStatus().name())
                .chatRoomId(match.getChatRoomId())
                .build();
    }
}
