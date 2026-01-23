package sparta.firstevent.adapter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventRequestDto {
    @Size(min=10, max=200, message = "title length is max 200")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotNull(message = "capacity is required")
    private Integer capacity;

    @NotNull(message = "startAt is required")
    private LocalDateTime startAt;

    @NotNull(message = "endAt is required")
    private LocalDateTime endAt;

    public EventRequestDto(
        String title, String description, Integer capacity,
        LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
