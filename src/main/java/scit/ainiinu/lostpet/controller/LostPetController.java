package scit.ainiinu.lostpet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.common.security.annotation.Public;
import scit.ainiinu.lostpet.domain.LostPetReportStatus;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeRequest;
import scit.ainiinu.lostpet.dto.LostPetAnalyzeResponse;
import scit.ainiinu.lostpet.dto.LostPetCreateRequest;
import scit.ainiinu.lostpet.dto.LostPetDetailResponse;
import scit.ainiinu.lostpet.dto.LostPetMatchApproveRequest;
import scit.ainiinu.lostpet.dto.LostPetMatchCandidateResponse;
import scit.ainiinu.lostpet.dto.LostPetMatchResponse;
import scit.ainiinu.lostpet.dto.LostPetResponse;
import scit.ainiinu.lostpet.dto.LostPetSummaryResponse;
import scit.ainiinu.lostpet.service.LostPetAnalyzeService;
import scit.ainiinu.lostpet.service.LostPetMatchApprovalService;
import scit.ainiinu.lostpet.service.LostPetMatchQueryService;
import scit.ainiinu.lostpet.service.LostPetService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lost-pets")
public class LostPetController {

    private final LostPetService lostPetService;
    private final LostPetAnalyzeService lostPetAnalyzeService;
    private final LostPetMatchQueryService lostPetMatchQueryService;
    private final LostPetMatchApprovalService lostPetMatchApprovalService;

    @PostMapping
    public ResponseEntity<ApiResponse<LostPetResponse>> create(
            @CurrentMember Long memberId,
            @Valid @RequestBody LostPetCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(lostPetService.create(memberId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<LostPetSummaryResponse>>> list(
            @CurrentMember Long memberId,
            @RequestParam(name = "status", required = false) LostPetReportStatus status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(SliceResponse.of(lostPetService.list(memberId, status, pageable))));
    }

    @GetMapping("/{lostPetId}")
    public ResponseEntity<ApiResponse<LostPetDetailResponse>> detail(
            @CurrentMember Long memberId,
            @PathVariable("lostPetId") Long lostPetId
    ) {
        return ResponseEntity.ok(ApiResponse.success(lostPetService.detail(memberId, lostPetId)));
    }

    @Public
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<LostPetAnalyzeResponse>> analyze(
            @Valid @RequestBody LostPetAnalyzeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(lostPetAnalyzeService.analyze(request)));
    }

    @Public
    @GetMapping("/{lostPetId}/match")
    public ResponseEntity<ApiResponse<SliceResponse<LostPetMatchCandidateResponse>>> matchCandidates(
            @PathVariable("lostPetId") Long lostPetId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(SliceResponse.of(
                lostPetMatchQueryService.findCandidates(lostPetId, pageable)
        )));
    }

    @PostMapping("/{lostPetId}/match")
    public ResponseEntity<ApiResponse<LostPetMatchResponse>> approveMatch(
            @CurrentMember Long memberId,
            @PathVariable("lostPetId") Long lostPetId,
            @Valid @RequestBody LostPetMatchApproveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                lostPetMatchApprovalService.approve(lostPetId, memberId, request)
        ));
    }
}
