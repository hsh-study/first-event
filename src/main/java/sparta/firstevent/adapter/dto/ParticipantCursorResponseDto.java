package sparta.firstevent.adapter.dto;

import java.util.List;

public record ParticipantCursorResponseDto(
    List<ParticipantResponseDto> contents,
    long totalCount,
    Long nextCursor
) {
    public static ParticipantCursorResponseDto of(List<ParticipantResponseDto> contents, long totalCount, Long nextCursor) {
        return new ParticipantCursorResponseDto(contents, totalCount, nextCursor);
    }
}
