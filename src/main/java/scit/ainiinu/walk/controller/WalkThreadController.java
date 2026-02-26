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
import scit.ainiinu.walk.dto.request.ThreadApplyRequest;
import scit.ainiinu.walk.dto.request.ThreadCreateRequest;
import scit.ainiinu.walk.dto.request.ThreadPatchRequest;
import scit.ainiinu.walk.dto.response.ThreadApplyResponse;
import scit.ainiinu.walk.dto.response.ThreadHotspotResponse;
import scit.ainiinu.walk.dto.response.ThreadMapResponse;
import scit.ainiinu.walk.dto.response.ThreadResponse;
import scit.ainiinu.walk.dto.response.ThreadSummaryResponse;
import scit.ainiinu.walk.service.WalkThreadService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WalkThreadController {

    private final WalkThreadService walkThreadService;

    @PostMapping("/threads")
    public ResponseEntity<ApiResponse<ThreadResponse>> createThread(
            @CurrentMember Long memberId,
            @Valid @RequestBody ThreadCreateRequest request
    ) {
        ThreadResponse response = walkThreadService.createThread(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/threads")
    public ResponseEntity<ApiResponse<SliceResponse<ThreadSummaryResponse>>> getThreads(
            @CurrentMember Long memberId,
            @PageableDefault(size = 20, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        SliceResponse<ThreadSummaryResponse> response = walkThreadService.getThreads(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/threads/map")
    public ResponseEntity<ApiResponse<List<ThreadMapResponse>>> getMapThreads(
            @CurrentMember Long memberId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double radius
    ) {
        List<ThreadMapResponse> response = walkThreadService.getMapThreads(memberId, latitude, longitude, radius);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ApiResponse<ThreadResponse>> getThread(
            @CurrentMember Long memberId,
            @PathVariable Long threadId
    ) {
        ThreadResponse response = walkThreadService.getThread(memberId, threadId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/threads/{threadId}")
    public ResponseEntity<ApiResponse<ThreadResponse>> updateThread(
            @CurrentMember Long memberId,
            @PathVariable Long threadId,
            @RequestBody ThreadPatchRequest request
    ) {
        ThreadResponse response = walkThreadService.updateThread(memberId, threadId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<ApiResponse<Void>> deleteThread(
            @CurrentMember Long memberId,
            @PathVariable Long threadId
    ) {
        walkThreadService.deleteThread(memberId, threadId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/threads/{threadId}/apply")
    public ResponseEntity<ApiResponse<ThreadApplyResponse>> applyThread(
            @CurrentMember Long memberId,
            @PathVariable Long threadId,
            @RequestBody ThreadApplyRequest request
    ) {
        ThreadApplyResponse response = walkThreadService.applyThread(memberId, threadId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/threads/{threadId}/apply")
    public ResponseEntity<ApiResponse<Void>> cancelApplyThread(
            @CurrentMember Long memberId,
            @PathVariable Long threadId
    ) {
        walkThreadService.cancelApplyThread(memberId, threadId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/threads/hotspot")
    public ResponseEntity<ApiResponse<List<ThreadHotspotResponse>>> getHotspots(
            @CurrentMember Long memberId,
            @RequestParam(defaultValue = "24") Integer hours
    ) {
        List<ThreadHotspotResponse> response = walkThreadService.getHotspots(hours);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
