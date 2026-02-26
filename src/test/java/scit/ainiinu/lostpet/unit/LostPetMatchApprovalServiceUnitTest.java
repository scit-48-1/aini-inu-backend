package scit.ainiinu.lostpet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scit.ainiinu.lostpet.domain.LostPetMatch;
import scit.ainiinu.lostpet.domain.LostPetReport;
import scit.ainiinu.lostpet.domain.Sighting;
import scit.ainiinu.lostpet.dto.LostPetMatchApproveRequest;
import scit.ainiinu.lostpet.dto.LostPetMatchResponse;
import scit.ainiinu.lostpet.integration.chat.ChatRoomDirectClient;
import scit.ainiinu.lostpet.repository.LostPetMatchRepository;
import scit.ainiinu.lostpet.repository.LostPetReportRepository;
import scit.ainiinu.lostpet.repository.SightingRepository;
import scit.ainiinu.lostpet.service.LostPetMatchApprovalService;

@ExtendWith(MockitoExtension.class)
class LostPetMatchApprovalServiceUnitTest {

    @Mock
    private LostPetReportRepository lostPetReportRepository;

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private LostPetMatchRepository lostPetMatchRepository;

    @Mock
    private ChatRoomDirectClient chatRoomDirectClient;

    @InjectMocks
    private LostPetMatchApprovalService lostPetMatchApprovalService;

    @Nested
    @DisplayName("매치 승인")
    class Approve {

        @Test
        @DisplayName("채팅 생성 성공 시 CHAT_LINKED 상태를 반환한다")
        void chatLinked() {
            LostPetReport report = LostPetReport.create(
                    10L, "Momo", "Poodle", "u", "d", LocalDateTime.now(), "Gangnam"
            );
            report.assignIdForTest(1L);
            Sighting sighting = Sighting.create(
                    22L, "u2", LocalDateTime.now(), "Yeoksam", "m"
            );
            sighting.assignIdForTest(2L);
            LostPetMatch match = LostPetMatch.create(report, sighting, new BigDecimal("0.9"));

            given(lostPetReportRepository.findById(1L)).willReturn(Optional.of(report));
            given(sightingRepository.findById(2L)).willReturn(Optional.of(sighting));
            given(lostPetMatchRepository.findByLostPetReportIdAndSightingId(1L, 2L)).willReturn(Optional.of(match));
            given(chatRoomDirectClient.createDirectRoom(22L)).willReturn(555L);
            given(lostPetMatchRepository.save(any(LostPetMatch.class))).willAnswer(invocation -> invocation.getArgument(0));

            LostPetMatchResponse response = lostPetMatchApprovalService.approve(
                    1L,
                    10L,
                    new LostPetMatchApproveRequest(2L)
            );

            assertThat(response.status()).isEqualTo("CHAT_LINKED");
            assertThat(response.chatRoomId()).isEqualTo(555L);
        }

        @Test
        @DisplayName("채팅 생성 실패 시 PENDING_CHAT_LINK 상태를 반환한다")
        void pendingOnChatFailure() {
            LostPetReport report = LostPetReport.create(
                    10L, "Momo", "Poodle", "u", "d", LocalDateTime.now(), "Gangnam"
            );
            report.assignIdForTest(1L);
            Sighting sighting = Sighting.create(
                    22L, "u2", LocalDateTime.now(), "Yeoksam", "m"
            );
            sighting.assignIdForTest(2L);
            LostPetMatch match = LostPetMatch.create(report, sighting, new BigDecimal("0.9"));

            given(lostPetReportRepository.findById(1L)).willReturn(Optional.of(report));
            given(sightingRepository.findById(2L)).willReturn(Optional.of(sighting));
            given(lostPetMatchRepository.findByLostPetReportIdAndSightingId(1L, 2L)).willReturn(Optional.of(match));
            given(chatRoomDirectClient.createDirectRoom(22L)).willThrow(new RuntimeException("chat down"));
            given(lostPetMatchRepository.save(any(LostPetMatch.class))).willAnswer(invocation -> invocation.getArgument(0));

            LostPetMatchResponse response = lostPetMatchApprovalService.approve(
                    1L,
                    10L,
                    new LostPetMatchApproveRequest(2L)
            );

            assertThat(response.status()).isEqualTo("PENDING_CHAT_LINK");
            assertThat(response.chatRoomId()).isNull();
        }
    }
}
