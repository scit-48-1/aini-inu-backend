package scit.ainiinu.community.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import scit.ainiinu.community.entity.Story;

import java.time.LocalDateTime;
import java.util.Collection;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Slice<Story> findByAuthorIdInAndCreatedAtAfterOrderByCreatedAtDescIdDesc(
            Collection<Long> authorIds,
            LocalDateTime createdAt,
            Pageable pageable
    );
}
