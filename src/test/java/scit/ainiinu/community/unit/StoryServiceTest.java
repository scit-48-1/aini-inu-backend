package scit.ainiinu.community.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import scit.ainiinu.common.response.SliceResponse;
import scit.ainiinu.community.dto.StoryResponse;
import scit.ainiinu.community.entity.Story;
import scit.ainiinu.community.repository.StoryRepository;
import scit.ainiinu.community.service.StoryService;
import scit.ainiinu.member.entity.Member;
import scit.ainiinu.member.entity.enums.MemberType;
import scit.ainiinu.member.repository.MemberFollowRepository;
import scit.ainiinu.member.repository.MemberRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private MemberFollowRepository memberFollowRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private StoryService storyService;

    @Nested
    @DisplayName("스토리 목록 조회")
    class GetStories {

        @Test
        @DisplayName("팔로잉 작성자의 24시간 이내 스토리만 Slice로 반환한다")
        void returnsStoriesWithin24HoursByFollowing() throws Exception {
            Long memberId = 1L;
            Long followingId = 10L;

            Story story = Story.create(followingId, "https://cdn.example.com/story.jpg", LocalDateTime.now().plusHours(24));
            setId(story, 100L);

            Slice<Story> storySlice = new SliceImpl<>(List.of(story), PageRequest.of(0, 20), false);

            Member author = Member.builder()
                    .email("walker@example.com")
                    .nickname("몽이아빠")
                    .profileImageUrl("https://cdn.example.com/profile.jpg")
                    .memberType(MemberType.PET_OWNER)
                    .build();
            setId(author, followingId);

            given(memberFollowRepository.findFollowingIdsByFollowerId(memberId)).willReturn(List.of(followingId));
            given(storyRepository.findByAuthorIdInAndCreatedAtAfterOrderByCreatedAtDescIdDesc(eq(List.of(followingId)), any(LocalDateTime.class), any()))
                    .willReturn(storySlice);
            given(memberRepository.findAllById(List.of(followingId))).willReturn(List.of(author));

            SliceResponse<StoryResponse> response = storyService.getStories(memberId, PageRequest.of(0, 20));

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getMemberId()).isEqualTo(followingId);
            assertThat(response.getContent().get(0).getNickname()).isEqualTo("몽이아빠");
        }
    }

    private static void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }
}
