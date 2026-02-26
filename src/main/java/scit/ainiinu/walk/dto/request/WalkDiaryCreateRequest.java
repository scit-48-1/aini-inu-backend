package scit.ainiinu.walk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WalkDiaryCreateRequest {

    private Long threadId;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String content;

    @Size(max = 5)
    private List<String> photoUrls;

    @NotNull
    private LocalDate walkDate;

    private Boolean isPublic;

    public boolean resolveIsPublic() {
        return isPublic == null || isPublic;
    }
}
