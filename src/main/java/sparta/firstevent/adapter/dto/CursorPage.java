package sparta.firstevent.adapter.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CursorPage<T> {

    private List<T> contents;

    private Long nextCursor;

    private Long totalElements;

    public CursorPage(List<T> contents, Long nextCursor, Long totalElements) {
    }
}
