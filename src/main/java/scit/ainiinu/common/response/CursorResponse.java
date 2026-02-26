package scit.ainiinu.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorResponse<T> {
    private List<T> content;
    private String nextCursor;
    private boolean hasMore;
}
