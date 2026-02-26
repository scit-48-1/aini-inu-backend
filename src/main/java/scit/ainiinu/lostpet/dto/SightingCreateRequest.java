package scit.ainiinu.lostpet.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SightingCreateRequest {

    @NotBlank
    private String photoUrl;

    @NotNull
    private LocalDateTime foundAt;

    @NotBlank
    private String foundLocation;

    private String memo;
}
