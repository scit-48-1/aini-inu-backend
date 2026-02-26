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
public class LostPetCreateRequest {

    @NotBlank
    private String petName;

    private String breed;

    @NotBlank
    private String photoUrl;

    private String description;

    @NotNull
    private LocalDateTime lastSeenAt;

    @NotBlank
    private String lastSeenLocation;
}
