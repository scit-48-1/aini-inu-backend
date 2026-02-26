package scit.ainiinu.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import scit.ainiinu.common.exception.BusinessException;
import scit.ainiinu.community.config.CommunityStorageProperties;
import scit.ainiinu.community.dto.ImageUploadResponse;
import scit.ainiinu.community.dto.UploadPurpose;
import scit.ainiinu.community.exception.CommunityErrorCode;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final CommunityStorageProperties properties;

    @Transactional
    public ImageUploadResponse uploadImage(Long memberId, String purposeValue, MultipartFile file) {
        validateRequest(file);

        UploadPurpose purpose = UploadPurpose.from(purposeValue);
        String contentType = file.getContentType();
        String objectKey = buildObjectKey(memberId, purpose, contentType);
        Path target = resolveAndValidatePath(objectKey);

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException(CommunityErrorCode.STORAGE_UNAVAILABLE, e);
        }

        return ImageUploadResponse.builder()
                .imageUrl(buildImageUrl(objectKey))
                .maxFileSizeBytes(MAX_FILE_SIZE_BYTES)
                .build();
    }

    public Resource getLocalImage(String key) {
        Path path = resolveAndValidatePath(key);
        if (!Files.exists(path)) {
            throw new BusinessException(CommunityErrorCode.UPLOAD_URL_EXPIRED_OR_INVALID);
        }
        return new FileSystemResource(path);
    }

    private void validateRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(CommunityErrorCode.INVALID_UPLOAD_PURPOSE);
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(CommunityErrorCode.FILE_SIZE_EXCEEDED);
        }

        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new BusinessException(CommunityErrorCode.INVALID_UPLOAD_MIME);
        }
    }

    private String buildObjectKey(Long memberId, UploadPurpose purpose, String contentType) {
        LocalDate today = LocalDate.now();
        String extension = resolveExtension(contentType);
        return String.join("/", List.of(
                "community",
                purpose.name().toLowerCase(),
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth()),
                String.valueOf(memberId),
                UUID.randomUUID() + "." + extension
        ));
    }

    private Path resolveAndValidatePath(String objectKey) {
        Path baseDir = Path.of(properties.getLocal().getBaseDir()).toAbsolutePath().normalize();
        Path resolved = baseDir.resolve(objectKey).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new BusinessException(CommunityErrorCode.INVALID_UPLOAD_PURPOSE);
        }
        return resolved;
    }

    private String buildImageUrl(String objectKey) {
        String baseUrl = properties.getPublicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        return baseUrl + "/api/v1/images/local?key=" + encodedKey;
    }

    private String resolveExtension(String contentType) {
        if ("image/png".equals(contentType)) {
            return "png";
        }
        if ("image/webp".equals(contentType)) {
            return "webp";
        }
        return "jpg";
    }
}
