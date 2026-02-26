package scit.ainiinu.walk.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import scit.ainiinu.common.security.jwt.JwtTokenProvider;
import scit.ainiinu.member.entity.Member;
import scit.ainiinu.member.entity.enums.MemberType;
import scit.ainiinu.member.repository.MemberRepository;
import scit.ainiinu.walk.dto.request.ThreadCreateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "animal.registry.api.key=test-key",
        "spring.datasource.url=jdbc:h2:mem:walkthread-int;MODE=MySQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@Transactional
class WalkThreadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("스레드 생성 후 목록 조회까지 통합 흐름이 동작한다")
    void createAndListThread_success() throws Exception {
        // given
        Member member = memberRepository.save(Member.builder()
                .email("thread-owner@test.com")
                .nickname("throwner1")
                .memberType(MemberType.PET_OWNER)
                .build());

        String token = jwtTokenProvider.generateAccessToken(member.getId());

        ThreadCreateRequest request = new ThreadCreateRequest();
        request.setTitle("한강 산책 모집");
        request.setDescription("저녁 산책 함께해요");
        request.setWalkDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setChatType("GROUP");
        request.setMaxParticipants(5);
        request.setAllowNonPetOwner(true);
        request.setIsVisibleAlways(true);
        ThreadCreateRequest.LocationRequest location = new ThreadCreateRequest.LocationRequest();
        location.setPlaceName("서울숲");
        location.setLatitude(37.54);
        location.setLongitude(127.04);
        location.setAddress("성동구");
        request.setLocation(location);
        request.setPetIds(List.of(1L));

        // when & then
        mockMvc.perform(post("/api/v1/threads")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        mockMvc.perform(get("/api/v1/threads")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }
}
