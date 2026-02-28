package scit.ainiinu.community.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.common.security.jwt.JwtTokenProvider;
import scit.ainiinu.community.entity.Story;
import scit.ainiinu.community.repository.StoryRepository;
import scit.ainiinu.member.entity.Member;
import scit.ainiinu.member.entity.MemberFollow;
import scit.ainiinu.member.entity.enums.MemberType;
import scit.ainiinu.member.repository.MemberFollowRepository;
import scit.ainiinu.member.repository.MemberRepository;
import scit.ainiinu.testsupport.IntegrationTestProfile;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@IntegrationTestProfile
class StoryListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberFollowRepository memberFollowRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Test
    @DisplayName("팔로잉 사용자의 24시간 내 스토리만 조회된다")
    void getStoriesWithin24Hours() throws Exception {
        Member me = memberRepository.save(Member.builder()
                .email("viewer@example.com")
                .nickname("viewer1")
                .memberType(MemberType.PET_OWNER)
                .build());

        Member author = memberRepository.save(Member.builder()
                .email("author@example.com")
                .nickname("author1")
                .memberType(MemberType.PET_OWNER)
                .build());

        memberFollowRepository.save(MemberFollow.builder()
                .followerId(me.getId())
                .followingId(author.getId())
                .build());

        storyRepository.save(Story.create(
                author.getId(),
                "https://cdn.example.com/story.jpg",
                LocalDateTime.now().plusHours(24)
        ));

        String accessToken = jwtTokenProvider.generateAccessToken(me.getId());

        mockMvc.perform(get("/api/v1/stories")
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].memberId").value(author.getId()));
    }
}
