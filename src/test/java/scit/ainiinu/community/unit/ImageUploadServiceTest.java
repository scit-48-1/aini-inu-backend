package scit.ainiinu.community.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import scit.ainiinu.common.exception.BusinessException;
import scit.ainiinu.community.config.CommunityStorageProperties;
import scit.ainiinu.community.dto.ImageUploadResponse;
import scit.ainiinu.community.exception.CommunityErrorCode;
import scit.ainiinu.community.service.ImageUploadService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ImageUploadServiceTest {

    private ImageUploadService imageUploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        CommunityStorageProperties properties = new CommunityStorageProperties();
        properties.setPublicBaseUrl("http://localhost:8080");
        properties.getLocal().setBaseDir(tempDir.toString());
        imageUploadService = new ImageUploadService(properties);
    }

    @Nested
    @DisplayName("로컬 이미지 업로드")
    class UploadImage {

        @Test
        @DisplayName("허용되지 않은 MIME 타입이면 예외가 발생한다")
        void failUnsupportedMimeType() {
            MultipartFile file = new MockMultipartFile("file", "sample.gif", "image/gif", "gif".getBytes());

            assertThatThrownBy(() -> imageUploadService.uploadImage(1L, "POST", file))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.INVALID_UPLOAD_MIME);
        }

        @Test
        @DisplayName("10MB 초과 파일이면 예외가 발생한다")
        void failOversizedFile() {
            MultipartFile file = mock(MultipartFile.class);
            given(file.isEmpty()).willReturn(false);
            given(file.getSize()).willReturn((10 * 1024 * 1024L) + 1);
            given(file.getContentType()).willReturn("image/jpeg");

            assertThatThrownBy(() -> imageUploadService.uploadImage(1L, "POST", file))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.FILE_SIZE_EXCEEDED);
        }

        @Test
        @DisplayName("유효한 요청이면 로컬 파일을 저장하고 imageUrl을 반환한다")
        void uploadSuccess() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "jpeg-data".getBytes());

            ImageUploadResponse response = imageUploadService.uploadImage(1L, "POST", file);

            assertThat(response.getImageUrl()).contains("/api/v1/images/local?key=");
            String encodedKey = response.getImageUrl().substring(response.getImageUrl().indexOf("key=") + 4);
            String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
            Path savedPath = tempDir.resolve(decodedKey);

            assertThat(Files.exists(savedPath)).isTrue();
            assertThat(Files.readAllBytes(savedPath)).isEqualTo("jpeg-data".getBytes());
        }
    }
}
