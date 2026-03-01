package scit.ainiinu.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.community.dto.StoryResponse;
import scit.ainiinu.community.service.StoryService;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
@Tag(name = "Community", description = "커뮤니티 API")
@SecurityRequirement(name = "bearerAuth")
public class StoryController {

    private final StoryService storyService;

    @GetMapping
    @Operation(summary = "스토리 목록 조회", description = "팔로잉 기반 스토리 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<SliceResponse<StoryResponse>>> getStories(
            @CurrentMember Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<StoryResponse> response = storyService.getStories(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
