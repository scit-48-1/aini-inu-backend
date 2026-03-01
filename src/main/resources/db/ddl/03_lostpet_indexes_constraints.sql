-- LostPet domain index/constraint alignment (PostgreSQL)

CREATE INDEX IF NOT EXISTS idx_lost_pet_report_owner_status
    ON lost_pet_report (owner_id, status);

CREATE INDEX IF NOT EXISTS idx_lost_pet_report_status_last_seen_at
    ON lost_pet_report (status, last_seen_at);

CREATE INDEX IF NOT EXISTS idx_sighting_finder_found_at
    ON sighting (finder_id, found_at);

CREATE INDEX IF NOT EXISTS idx_sighting_status_found_at
    ON sighting (status, found_at);

CREATE INDEX IF NOT EXISTS idx_lost_pet_match_lost_pet_status
    ON lost_pet_match (lost_pet_id, status);

CREATE INDEX IF NOT EXISTS idx_lost_pet_match_sighting_status
    ON lost_pet_match (sighting_id, status);

CREATE INDEX IF NOT EXISTS idx_lost_pet_search_session_owner_lost_pet_created
    ON lost_pet_search_session (owner_id, lost_pet_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_lost_pet_search_session_expires_at
    ON lost_pet_search_session (expires_at);

CREATE INDEX IF NOT EXISTS idx_lost_pet_search_candidate_session_rank
    ON lost_pet_search_candidate (session_id, rank_order);

CREATE INDEX IF NOT EXISTS idx_lost_pet_search_candidate_session_score
    ON lost_pet_search_candidate (session_id, score_total DESC, rank_order ASC);

CREATE INDEX IF NOT EXISTS idx_lostpet_vector_store_embedding_hnsw
    ON lostpet_vector_store
    USING HNSW (embedding vector_cosine_ops);
