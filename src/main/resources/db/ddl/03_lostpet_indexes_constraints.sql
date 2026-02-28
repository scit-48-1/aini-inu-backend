-- LostPet domain index/constraint alignment
ALTER TABLE lost_pet_match
    ADD CONSTRAINT uk_lost_pet_match_pair UNIQUE (lost_pet_id, sighting_id);

CREATE INDEX idx_lost_pet_report_owner_status
    ON lost_pet_report (owner_id, status);

CREATE INDEX idx_lost_pet_report_status_last_seen_at
    ON lost_pet_report (status, last_seen_at);

CREATE INDEX idx_sighting_finder_found_at
    ON sighting (finder_id, found_at);

CREATE INDEX idx_sighting_status_found_at
    ON sighting (status, found_at);

CREATE INDEX idx_lost_pet_match_lost_pet_status
    ON lost_pet_match (lost_pet_id, status);

CREATE INDEX idx_lost_pet_match_sighting_status
    ON lost_pet_match (sighting_id, status);
