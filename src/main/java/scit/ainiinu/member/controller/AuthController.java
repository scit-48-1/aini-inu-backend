package scit.ainiinu.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.security.annotation.Public;
import scit.ainiinu.member.dto.request.AuthLoginRequest;
import scit.ainiinu.member.dto.request.TokenRefreshRequest;
import scit.ainiinu.member.dto.request.TokenRevokeRequest;
import scit.ainiinu.member.dto.response.LoginResponse;
import scit.ainiinu.member.service.AuthService;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일 로그인 API
     */
    @Public
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody AuthLoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.loginWithEmail(request)));
    }

    /**
     * 토큰 갱신 API
     * Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.
     */
    @Public
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody TokenRevokeRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
