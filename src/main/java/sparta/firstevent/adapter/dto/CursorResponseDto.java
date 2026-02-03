package sparta.firstevent.adapter.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CursorResponseDto<T> {
    private List<T> contents;
    private Long nextCursor;
}
