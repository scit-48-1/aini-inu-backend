package scit.ainiinu.walk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.walk.dto.request.WalkDiaryCreateRequest;
import scit.ainiinu.walk.dto.request.WalkDiaryPatchRequest;
import scit.ainiinu.walk.dto.response.WalkDiaryResponse;
import scit.ainiinu.walk.service.WalkDiaryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalkDiaryController {

    private final WalkDiaryService walkDiaryService;

    @PostMapping("/walk-diaries")
    public ResponseEntity<ApiResponse<WalkDiaryResponse>> createDiary(
            @CurrentMember Long memberId,
            @Valid @RequestBody WalkDiaryCreateRequest request
    ) {
        WalkDiaryResponse response = walkDiaryService.createDiary(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/walk-diaries")
    public ResponseEntity<ApiResponse<SliceResponse<WalkDiaryResponse>>> getWalkDiaries(
            @CurrentMember Long memberId,
            @RequestParam(value = "memberId", required = false) Long targetMemberId,
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<WalkDiaryResponse> response = walkDiaryService.getWalkDiaries(memberId, targetMemberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/walk-diaries/following")
    public ResponseEntity<ApiResponse<SliceResponse<WalkDiaryResponse>>> getFollowingDiaries(
            @CurrentMember Long memberId,
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<WalkDiaryResponse> response = walkDiaryService.getFollowingDiaries(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/walk-diaries/{diaryId}")
    public ResponseEntity<ApiResponse<WalkDiaryResponse>> getDiary(
            @CurrentMember Long memberId,
            @PathVariable Long diaryId
    ) {
        WalkDiaryResponse response = walkDiaryService.getDiary(memberId, diaryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/walk-diaries/{diaryId}")
    public ResponseEntity<ApiResponse<WalkDiaryResponse>> updateDiary(
            @CurrentMember Long memberId,
            @PathVariable Long diaryId,
            @RequestBody WalkDiaryPatchRequest request
    ) {
        WalkDiaryResponse response = walkDiaryService.updateDiary(memberId, diaryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/walk-diaries/{diaryId}")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @CurrentMember Long memberId,
            @PathVariable Long diaryId
    ) {
        walkDiaryService.deleteDiary(memberId, diaryId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
