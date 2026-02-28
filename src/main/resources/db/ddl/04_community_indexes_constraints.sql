-- Community domain index/constraint alignment
CREATE INDEX idx_post_created_at_id
    ON post (created_at, id);

CREATE INDEX idx_post_author_created_at
    ON post (author_id, created_at);

CREATE INDEX idx_comment_post_created_at
    ON comment (post_id, created_at);

CREATE INDEX idx_story_expires_at
    ON story (expires_at);
