package scit.ainiinu.community.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.common.security.annotation.Public;
import scit.ainiinu.community.dto.PresignedImageRequest;
import scit.ainiinu.community.dto.PresignedImageResponse;
import scit.ainiinu.community.service.ImageUploadService;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedImageResponse>> createPresignedUrl(
            @CurrentMember Long memberId,
            @Valid @RequestBody PresignedImageRequest request
    ) {
        PresignedImageResponse response = imageUploadService.createPresignedUrl(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Public
    @PutMapping("/presigned-upload/{token}")
    public ResponseEntity<ApiResponse<Void>> uploadByPresignedUrl(
            @PathVariable String token,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            @RequestBody byte[] payload
    ) {
        imageUploadService.uploadByToken(token, contentType, payload);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Public
    @GetMapping(value = "/local")
    public ResponseEntity<Resource> getLocalFile(@RequestParam("key") String key) {
        Resource resource = imageUploadService.getLocalImage(key);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
