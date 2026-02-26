package scit.ainiinu.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.chat.dto.request.WalkConfirmRequest;
import scit.ainiinu.chat.dto.response.WalkConfirmResponse;
import scit.ainiinu.chat.entity.ChatParticipant;
import scit.ainiinu.chat.entity.ChatWalkConfirmState;
import scit.ainiinu.chat.exception.ChatErrorCode;
import scit.ainiinu.chat.exception.ChatException;
import scit.ainiinu.chat.repository.ChatParticipantRepository;
import scit.ainiinu.chat.repository.ChatRoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkConfirmService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    public WalkConfirmResponse updateWalkConfirm(Long memberId, Long chatRoomId, WalkConfirmRequest request) {
        chatRoomRepository.findByIdForUpdate(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_NOT_FOUND));

        ChatParticipant me = chatParticipantRepository.findByChatRoomIdAndMemberIdAndLeftAtIsNull(chatRoomId, memberId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.ROOM_ACCESS_DENIED));

        String action = request.getAction() == null ? "" : request.getAction().trim().toUpperCase();
        switch (action) {
            case "CONFIRM" -> me.confirmWalk();
            case "CANCEL" -> me.cancelWalkConfirm();
            default -> throw new ChatException(ChatErrorCode.INVALID_WALK_CONFIRM_ACTION);
        }

        List<ChatParticipant> activeParticipants = chatParticipantRepository.findAllByChatRoomIdAndLeftAtIsNull(chatRoomId);
        boolean allConfirmed = !activeParticipants.isEmpty()
                && activeParticipants.stream().allMatch(p -> p.getWalkConfirmState() == ChatWalkConfirmState.CONFIRMED);

        chatRoomRepository.findById(chatRoomId)
                .ifPresent(room -> room.updateWalkConfirmed(allConfirmed));

        List<Long> confirmedMemberIds = activeParticipants.stream()
                .filter(participant -> participant.getWalkConfirmState() == ChatWalkConfirmState.CONFIRMED)
                .map(ChatParticipant::getMemberId)
                .toList();

        return WalkConfirmResponse.builder()
                .roomId(chatRoomId)
                .memberId(memberId)
                .myState(me.getWalkConfirmState().name())
                .allConfirmed(allConfirmed)
                .confirmedMemberIds(confirmedMemberIds)
                .build();
    }
}
