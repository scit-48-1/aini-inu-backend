-- Chat domain index/constraint alignment
CREATE INDEX idx_chat_room_thread_id
    ON chat_room (thread_id);
