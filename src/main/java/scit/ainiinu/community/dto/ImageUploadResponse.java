package scit.ainiinu.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponse {
    private String imageUrl;
    private Long maxFileSizeBytes;
}
