package scit.ainiinu.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scit.ainiinu.member.entity.Member;
import scit.ainiinu.member.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByMember(Member member);
    Optional<RefreshToken> findByMemberId(Long memberId);
}
