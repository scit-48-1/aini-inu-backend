package scit.ainiinu.community.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.common.security.annotation.Public;
import scit.ainiinu.community.dto.ImageUploadResponse;
import scit.ainiinu.community.service.ImageUploadService;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @CurrentMember Long memberId,
            @RequestParam("purpose") String purpose,
            @RequestParam("file") MultipartFile file
    ) {
        ImageUploadResponse response = imageUploadService.uploadImage(memberId, purpose, file);
        return ResponseEntity.ok(ApiResponse.success(response));
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
