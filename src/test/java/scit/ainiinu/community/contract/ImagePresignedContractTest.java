package scit.ainiinu.community.contract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scit.ainiinu.common.exception.BusinessException;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.common.security.interceptor.JwtAuthInterceptor;
import scit.ainiinu.common.security.resolver.CurrentMemberArgumentResolver;
import scit.ainiinu.community.controller.ImageController;
import scit.ainiinu.community.dto.ImageUploadResponse;
import scit.ainiinu.community.exception.CommunityErrorCode;
import scit.ainiinu.community.service.ImageUploadService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImagePresignedContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @MockitoBean
    private JwtAuthInterceptor jwtAuthInterceptor;

    @MockitoBean
    private CurrentMemberArgumentResolver currentMemberArgumentResolver;

    @BeforeEach
    void setUp() throws Exception {
        given(jwtAuthInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(currentMemberArgumentResolver.supportsParameter(
                argThat(parameter -> parameter.hasParameterAnnotation(CurrentMember.class))
        )).willReturn(true);
        given(currentMemberArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 업로드 요청이 유효하면 200 응답을 반환한다")
    void uploadImageSuccess() throws Exception {
        ImageUploadResponse response = ImageUploadResponse.builder()
                .imageUrl("http://localhost:8080/api/v1/images/local?key=community/post/file.jpg")
                .maxFileSizeBytes(10 * 1024 * 1024L)
                .build();

        given(imageUploadService.uploadImage(anyLong(), any(), any()))
                .willReturn(response);

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file("file", "binary-image".getBytes())
                        .param("purpose", "POST")
                        .with(csrf())
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.imageUrl").value("http://localhost:8080/api/v1/images/local?key=community/post/file.jpg"))
                .andExpect(jsonPath("$.data.maxFileSizeBytes").value(10 * 1024 * 1024));
    }

    @Test
    @WithMockUser
    @DisplayName("허용되지 않은 MIME 타입이면 415 에러를 반환한다")
    void uploadImageInvalidMime() throws Exception {
        given(imageUploadService.uploadImage(anyLong(), any(), any()))
                .willThrow(new BusinessException(CommunityErrorCode.INVALID_UPLOAD_MIME));

        mockMvc.perform(multipart("/api/v1/images/upload")
                        .file("file", "binary-image".getBytes())
                        .param("purpose", "POST")
                        .with(csrf())
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CO006"));
    }
}
