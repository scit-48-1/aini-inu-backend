package scit.ainiinu.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scit.ainiinu.chat.dto.request.ChatMessageCreateRequest;
import scit.ainiinu.chat.dto.request.ChatReviewCreateRequest;
import scit.ainiinu.chat.dto.request.ChatRoomDirectCreateRequest;
import scit.ainiinu.chat.dto.request.MessageReadRequest;
import scit.ainiinu.chat.dto.request.WalkConfirmRequest;
import scit.ainiinu.chat.dto.response.ChatMessageResponse;
import scit.ainiinu.chat.dto.response.ChatReviewResponse;
import scit.ainiinu.chat.dto.response.ChatRoomDetailResponse;
import scit.ainiinu.chat.dto.response.ChatRoomSummaryResponse;
import scit.ainiinu.chat.dto.response.LeaveRoomResponse;
import scit.ainiinu.chat.dto.response.MessageReadResponse;
import scit.ainiinu.chat.dto.response.MyChatReviewResponse;
import scit.ainiinu.chat.dto.response.WalkConfirmResponse;
import scit.ainiinu.chat.service.ChatReviewService;
import scit.ainiinu.chat.service.ChatRoomService;
import scit.ainiinu.chat.service.MessageService;
import scit.ainiinu.chat.service.WalkConfirmService;
import scit.ainiinu.common.response.ApiResponse;
import scit.ainiinu.common.response.CursorResponse;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.common.security.annotation.CurrentMember;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-rooms")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final WalkConfirmService walkConfirmService;
    private final ChatReviewService chatReviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<ChatRoomSummaryResponse>>> getChatRooms(
            @CurrentMember Long memberId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        SliceResponse<ChatRoomSummaryResponse> response = chatRoomService.getRooms(memberId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> createDirectRoom(
            @CurrentMember Long memberId,
            @Valid @RequestBody ChatRoomDirectCreateRequest request
    ) {
        ChatRoomDetailResponse response = chatRoomService.createDirectRoom(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoom(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId
    ) {
        ChatRoomDetailResponse response = chatRoomService.getRoomDetail(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<CursorResponse<ChatMessageResponse>>> getMessages(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "before") String direction
    ) {
        CursorResponse<ChatMessageResponse> response = messageService.getMessages(memberId, chatRoomId, cursor, size, direction);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> createMessage(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatMessageCreateRequest request
    ) {
        ChatMessageResponse response = messageService.createMessage(memberId, chatRoomId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatRoomId}/messages/read")
    public ResponseEntity<ApiResponse<MessageReadResponse>> readMessage(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody MessageReadRequest request
    ) {
        MessageReadResponse response = messageService.markRead(memberId, chatRoomId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatRoomId}/leave")
    public ResponseEntity<ApiResponse<LeaveRoomResponse>> leaveRoom(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId
    ) {
        LeaveRoomResponse response = chatRoomService.leaveRoom(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatRoomId}/walk-confirm")
    public ResponseEntity<ApiResponse<WalkConfirmResponse>> walkConfirm(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @RequestBody(required = false) WalkConfirmRequest request
    ) {
        WalkConfirmResponse response;
        if (request == null || request.getAction() == null || request.getAction().isBlank()) {
            response = walkConfirmService.confirmWalk(memberId, chatRoomId);
        } else {
            response = walkConfirmService.updateWalkConfirm(memberId, chatRoomId, request);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/walk-confirm")
    public ResponseEntity<ApiResponse<WalkConfirmResponse>> getWalkConfirm(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId
    ) {
        WalkConfirmResponse response = walkConfirmService.getWalkConfirm(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{chatRoomId}/walk-confirm")
    public ResponseEntity<ApiResponse<Void>> cancelWalkConfirm(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId
    ) {
        walkConfirmService.cancelWalkConfirm(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{chatRoomId}/reviews/me")
    public ResponseEntity<ApiResponse<MyChatReviewResponse>> getMyReview(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId
    ) {
        MyChatReviewResponse response = chatReviewService.getMyReview(memberId, chatRoomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{chatRoomId}/reviews")
    public ResponseEntity<ApiResponse<ChatReviewResponse>> createReview(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody ChatReviewCreateRequest request
    ) {
        ChatReviewResponse response = chatReviewService.createReview(memberId, chatRoomId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{chatRoomId}/reviews")
    public ResponseEntity<ApiResponse<SliceResponse<ChatReviewResponse>>> getReviews(
            @CurrentMember Long memberId,
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        SliceResponse<ChatReviewResponse> response = chatReviewService.getReviews(memberId, chatRoomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
