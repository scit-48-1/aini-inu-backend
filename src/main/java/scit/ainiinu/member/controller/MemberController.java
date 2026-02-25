package scit.ainiinu.member.controller;

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
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;
import scit.ainiinu.common.security.annotation.Public;
import scit.ainiinu.member.dto.request.MemberCreateRequest;
import scit.ainiinu.member.dto.request.MemberProfilePatchRequest;
import scit.ainiinu.member.dto.request.MemberSignupRequest;
import scit.ainiinu.member.dto.response.FollowStatusResponse;
import scit.ainiinu.member.dto.response.LoginResponse;
import scit.ainiinu.member.dto.response.MemberFollowResponse;
import scit.ainiinu.member.dto.response.MemberResponse;
import scit.ainiinu.member.service.AuthService;
import scit.ainiinu.member.service.MemberService;
import scit.ainiinu.pet.dto.response.PetResponse;
import scit.ainiinu.pet.service.PetService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final PetService petService;
    private final AuthService authService;

    /**
     * 회원가입 완료 (프로필 생성)
     * 가입 직후 추가 프로필 정보를 입력받아 가입을 완료합니다.
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<MemberResponse>> createProfile(
            @CurrentMember Long memberId,
            @Valid @RequestBody MemberCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.createProfile(memberId, request)));
    }

    @Public
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(
            @Valid @RequestBody MemberSignupRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyProfile(
            @CurrentMember Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyProfile(memberId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyProfile(
            @CurrentMember Long memberId,
            @Valid @RequestBody MemberProfilePatchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMyProfile(memberId, request)));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberProfile(
            @PathVariable("memberId") Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberProfile(memberId)));
    }

    @GetMapping("/{memberId}/pets")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getMemberPets(
            @PathVariable("memberId") Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(petService.getUserPets(memberId)));
    }

    @GetMapping("/me/followers")
    public ResponseEntity<ApiResponse<SliceResponse<MemberFollowResponse>>> getFollowers(
            @CurrentMember Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getFollowers(memberId, pageable)));
    }

    @GetMapping("/me/following")
    public ResponseEntity<ApiResponse<SliceResponse<MemberFollowResponse>>> getFollowing(
            @CurrentMember Long memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getFollowing(memberId, pageable)));
    }

    @PostMapping("/me/follows/{targetId}")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> follow(
            @CurrentMember Long memberId,
            @PathVariable("targetId") Long targetId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.follow(memberId, targetId)));
    }

    @DeleteMapping("/me/follows/{targetId}")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> unfollow(
            @CurrentMember Long memberId,
            @PathVariable("targetId") Long targetId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.unfollow(memberId, targetId)));
    }

    @GetMapping("/me/stats/walk")
    public ResponseEntity<ApiResponse<int[]>> getWalkStats(
            @CurrentMember Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getWalkStats(memberId)));
    }
}
