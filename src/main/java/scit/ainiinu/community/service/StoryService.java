package scit.ainiinu.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.community.dto.StoryResponse;
import scit.ainiinu.community.entity.Story;
import scit.ainiinu.community.repository.StoryRepository;
import scit.ainiinu.member.entity.Member;
import scit.ainiinu.member.repository.MemberFollowRepository;
import scit.ainiinu.member.repository.MemberRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;
    private final MemberFollowRepository memberFollowRepository;
    private final MemberRepository memberRepository;

    public SliceResponse<StoryResponse> getStories(Long memberId, Pageable pageable) {
        List<Long> followingIds = memberFollowRepository.findFollowingIdsByFollowerId(memberId);
        if (followingIds.isEmpty()) {
            Slice<StoryResponse> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);
            return SliceResponse.of(emptySlice);
        }

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        Slice<Story> storySlice = storyRepository.findByAuthorIdInAndCreatedAtAfterOrderByCreatedAtDescIdDesc(
                followingIds,
                cutoff,
                pageable
        );

        List<Long> authorIds = storySlice.getContent().stream()
                .map(Story::getAuthorId)
                .distinct()
                .toList();

        Map<Long, Member> memberMap = memberRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        Slice<StoryResponse> mappedSlice = storySlice.map(story -> {
            Member author = memberMap.get(story.getAuthorId());
            LocalDateTime createdAt = story.getCreatedAt() != null ? story.getCreatedAt() : LocalDateTime.now();
            return StoryResponse.builder()
                    .id(story.getId())
                    .memberId(story.getAuthorId())
                    .nickname(author != null ? author.getNickname() : "이웃")
                    .profileImageUrl(author != null ? author.getProfileImageUrl() : null)
                    .coverImageUrl(story.getMediaUrl())
                    .walkDate(createdAt.toLocalDate())
                    .createdAt(createdAt.atOffset(ZoneOffset.UTC))
                    .build();
        });

        return SliceResponse.of(mappedSlice);
    }
}
