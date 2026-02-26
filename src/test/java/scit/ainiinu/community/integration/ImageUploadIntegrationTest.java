package scit.ainiinu.community.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.common.security.jwt.JwtTokenProvider;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "community.storage.local.base-dir=${java.io.tmpdir}/aini-inu-test-uploads",
        "spring.sql.init.mode=never",
        "lostpet.ai.base-url=http://localhost:18080",
        "lostpet.ai.analyze-path=/api/v1/analyze",
        "lostpet.chat.base-url=http://localhost:18081",
        "lostpet.chat.direct-create-path=/api/v1/chat/rooms/direct"
})
class ImageUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("유효한 multipart 요청으로 이미지를 업로드한다")
    void uploadImage() throws Exception {
        String accessToken = jwtTokenProvider.generateAccessToken(1L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.jpg",
                "image/jpeg",
                "image-bytes".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .param("purpose", "POST")
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").exists())
                .andExpect(jsonPath("$.data.maxFileSizeBytes").value(10 * 1024 * 1024));
    }

    @Test
    @DisplayName("허용되지 않은 MIME 타입 업로드는 415를 반환한다")
    void uploadImageInvalidMime() throws Exception {
        String accessToken = jwtTokenProvider.generateAccessToken(1L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.gif",
                "image/gif",
                "gif-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file(file)
                        .param("purpose", "POST")
                        .with(csrf())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CO006"));
    }
}
