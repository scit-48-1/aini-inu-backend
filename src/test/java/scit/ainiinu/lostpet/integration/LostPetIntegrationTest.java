package scit.ainiinu.lostpet.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.common.security.jwt.JwtTokenProvider;
import scit.ainiinu.lostpet.integration.ai.LostPetAiClient;
import scit.ainiinu.lostpet.integration.ai.LostPetAiResult;
import scit.ainiinu.lostpet.integration.chat.ChatRoomDirectClient;
import scit.ainiinu.testsupport.IntegrationTestProfile;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@IntegrationTestProfile
class LostPetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LostPetAiClient lostPetAiClient;

    @MockitoBean
    private ChatRoomDirectClient chatRoomDirectClient;

    @Nested
    @DisplayName("실종 신고 플로우")
    class LostReportFlow {

        @Test
        @DisplayName("실종 신고 생성 후 목록/상세 조회가 가능하다")
        void createListDetail() throws Exception {
            Long memberId = 10L;
            String token = jwtTokenProvider.generateAccessToken(memberId);

            String createRequest = """
                    {
                      "petName": "Momo",
                      "breed": "Poodle",
                      "photoUrl": "https://cdn/momo.jpg",
                      "description": "desc",
                      "lastSeenAt": "2026-02-26T10:00:00",
                      "lastSeenLocation": "Gangnam"
                    }
                    """;

            MvcResult createResult = mockMvc.perform(post("/api/v1/lost-pets")
                            .with(csrf())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.lostPetId").exists())
                    .andReturn();

            mockMvc.perform(get("/api/v1/lost-pets")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].petName").value("Momo"));

            JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
            long lostPetId = createNode.path("data").path("lostPetId").asLong();

            mockMvc.perform(get("/api/v1/lost-pets/" + lostPetId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.lostPetId").value(lostPetId));
        }
    }

    @Nested
    @DisplayName("AI/매치 플로우")
    class AnalyzeAndMatchFlow {

        @Test
        @DisplayName("AI 예외 발생 시에도 200 + fallback 응답을 반환한다")
        void analyzeFallbackResponse() throws Exception {
            given(lostPetAiClient.analyze(any())).willThrow(new RuntimeException("timeout"));

            String analyzeRequest = """
                    {
                      "imageUrl": "https://cdn/unknown.jpg",
                      "mode": "LOST"
                    }
                    """;

            mockMvc.perform(post("/api/v1/lost-pets/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(analyzeRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fallback").value(true))
                    .andExpect(jsonPath("$.data.candidates").isArray());
        }

        @Test
        @DisplayName("AI 성공 시 후보와 요약을 반환한다")
        void analyzeSuccessResponse() throws Exception {
            given(lostPetAiClient.analyze(any()))
                    .willReturn(new LostPetAiResult("ok", List.of()));

            String analyzeRequest = """
                    {
                      "imageUrl": "https://cdn/unknown.jpg",
                      "mode": "LOST"
                    }
                    """;

            mockMvc.perform(post("/api/v1/lost-pets/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(analyzeRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fallback").value(false))
                    .andExpect(jsonPath("$.data.summary").value("ok"));
        }
    }
}
