package scit.ainiinu.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class StoryResponse {
    private Long id;
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private String coverImageUrl;
    private LocalDate walkDate;
    private OffsetDateTime createdAt;
}
