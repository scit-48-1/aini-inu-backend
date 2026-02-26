package scit.ainiinu.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scit.ainiinu.common.entity.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "story")
public class Story extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public static Story create(Long authorId, String mediaUrl, LocalDateTime expiresAt) {
        Story story = new Story();
        story.authorId = authorId;
        story.mediaUrl = mediaUrl;
        story.expiresAt = expiresAt;
        return story;
    }
}
