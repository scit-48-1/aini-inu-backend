package scit.ainiinu.lostpet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import scit.ainiinu.lostpet.domain.LostPetReport;
import scit.ainiinu.lostpet.domain.LostPetSearchSession;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeResponse;
import scit.ainiinu.lostpet.integration.ai.LostPetAiClient;
import scit.ainiinu.lostpet.integration.ai.LostPetAiResult;
import scit.ainiinu.lostpet.repository.LostPetReportRepository;
import scit.ainiinu.lostpet.repository.LostPetSearchCandidateRepository;
import scit.ainiinu.lostpet.repository.LostPetSearchSessionRepository;
import scit.ainiinu.lostpet.repository.SightingRepository;
import scit.ainiinu.lostpet.service.LostPetCandidateScoringService;
import scit.ainiinu.lostpet.service.LostPetAnalyzeService;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LostPetAnalyzeServiceUnitTest {

    @Mock
    private LostPetAiClient lostPetAiClient;

    @Mock
    private LostPetReportRepository lostPetReportRepository;

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private LostPetSearchSessionRepository lostPetSearchSessionRepository;

    @Mock
    private LostPetSearchCandidateRepository lostPetSearchCandidateRepository;

    @Mock
    private LostPetCandidateScoringService lostPetCandidateScoringService;

    @InjectMocks
    private LostPetAnalyzeService lostPetAnalyzeService;

    @Nested
    @DisplayName("AI 분석")
    class Analyze {

        @Test
        @DisplayName("외부 분석이 성공하면 후보를 반환한다")
        void success() {
            LostPetReport report = LostPetReport.create(
                    1L,
                    "Momo",
                    "Poodle",
                    "https://cdn/momo.jpg",
                    "desc",
                    LocalDateTime.now(),
                    "Gangnam"
            );
            report.assignIdForTest(10L);
            LostPetSearchSession session = LostPetSearchSession.create(
                    1L,
                    report,
                    "LOST",
                    "https://cdn/sample.jpg",
                    null,
                    LocalDateTime.now().plusHours(24)
            );
            ReflectionTestUtils.setField(session, "id", 101L);

            given(lostPetReportRepository.findById(anyLong())).willReturn(Optional.of(report));
            given(lostPetSearchSessionRepository.save(any(LostPetSearchSession.class))).willReturn(session);
            given(lostPetAiClient.analyze(any()))
                    .willReturn(new LostPetAiResult("ok", List.of()));
            LostPetAnalyzeRequest request = LostPetAnalyzeRequest.builder()
                    .lostPetId(10L)
                    .imageUrl("https://cdn/sample.jpg")
                    .mode("LOST")
                    .build();

            LostPetAnalyzeResponse response = lostPetAnalyzeService.analyze(1L, request);

            assertThat(response.fallback()).isFalse();
            assertThat(response.summary()).isEqualTo("ok");
            assertThat(response.sessionId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("외부 분석 실패 시 fallback 결과를 반환한다")
        void fallback() {
            LostPetReport report = LostPetReport.create(
                    1L,
                    "Momo",
                    "Poodle",
                    "https://cdn/momo.jpg",
                    "desc",
                    LocalDateTime.now(),
                    "Gangnam"
            );
            report.assignIdForTest(10L);
            LostPetSearchSession session = LostPetSearchSession.create(
                    1L,
                    report,
                    "LOST",
                    "https://cdn/sample.jpg",
                    null,
                    LocalDateTime.now().plusHours(24)
            );
            ReflectionTestUtils.setField(session, "id", 102L);

            given(lostPetReportRepository.findById(anyLong())).willReturn(Optional.of(report));
            given(lostPetSearchSessionRepository.save(any(LostPetSearchSession.class))).willReturn(session);
            given(lostPetAiClient.analyze(any())).willThrow(new RuntimeException("timeout"));
            LostPetAnalyzeRequest request = LostPetAnalyzeRequest.builder()
                    .lostPetId(10L)
                    .imageUrl("https://cdn/sample.jpg")
                    .mode("LOST")
                    .build();

            LostPetAnalyzeResponse response = lostPetAnalyzeService.analyze(1L, request);

            assertThat(response.fallback()).isTrue();
            assertThat(response.candidates()).isEmpty();
            assertThat(response.sessionId()).isEqualTo(102L);
        }
    }
}
