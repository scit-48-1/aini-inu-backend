package scit.ainiinu.lostpet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeResponse;
import scit.ainiinu.lostpet.integration.ai.LostPetAiClient;
import scit.ainiinu.lostpet.integration.ai.LostPetAiResult;
import scit.ainiinu.lostpet.service.LostPetAnalyzeService;

@ExtendWith(MockitoExtension.class)
class LostPetAnalyzeServiceUnitTest {

    @Mock
    private LostPetAiClient lostPetAiClient;

    @InjectMocks
    private LostPetAnalyzeService lostPetAnalyzeService;

    @Nested
    @DisplayName("AI 분석")
    class Analyze {

        @Test
        @DisplayName("외부 분석이 성공하면 후보를 반환한다")
        void success() {
            given(lostPetAiClient.analyze(any()))
                    .willReturn(new LostPetAiResult("ok", List.of()));
            LostPetAnalyzeRequest request = LostPetAnalyzeRequest.builder()
                    .imageUrl("https://cdn/sample.jpg")
                    .mode("LOST")
                    .build();

            LostPetAnalyzeResponse response = lostPetAnalyzeService.analyze(request);

            assertThat(response.fallback()).isFalse();
            assertThat(response.summary()).isEqualTo("ok");
        }

        @Test
        @DisplayName("외부 분석 실패 시 fallback 결과를 반환한다")
        void fallback() {
            given(lostPetAiClient.analyze(any())).willThrow(new RuntimeException("timeout"));
            LostPetAnalyzeRequest request = LostPetAnalyzeRequest.builder()
                    .imageUrl("https://cdn/sample.jpg")
                    .mode("LOST")
                    .build();

            LostPetAnalyzeResponse response = lostPetAnalyzeService.analyze(request);

            assertThat(response.fallback()).isTrue();
            assertThat(response.candidates()).isEmpty();
        }
    }
}
