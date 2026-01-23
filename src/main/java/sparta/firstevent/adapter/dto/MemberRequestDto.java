package sparta.firstevent.adapter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record MemberRequestDto (
    @Email String email,
    @Size(min=8, max=30) String password,
    @Size(min=3, max=15) String nickname
) {

}
