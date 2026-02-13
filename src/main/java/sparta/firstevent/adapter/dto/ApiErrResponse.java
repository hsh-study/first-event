package sparta.firstevent.adapter.dto;

import lombok.Getter;

@Getter
public class ApiErrResponse {
    private String message;
    private int status;
    private String error;
}
