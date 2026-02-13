package sparta.firstevent.adapter.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import sparta.firstevent.adapter.dto.ApiErrResponse;
import sparta.firstevent.adapter.dto.EventResponseDto;

@Tag(name = "사용자 이벤트", description = "사용자 이벤트 관련 APIs")
public interface EventApi {

    @Operation(summary = "이벤트 목록 조회", description = "이벤트 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공")
    })
    Page<EventResponseDto> getAll(Pageable pageable);

    @Operation(summary = "이벤트 상세 조회", description = "eventId로 이벤트를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "이벤트 없음",
            content = @Content(schema = @Schema(implementation = ApiErrResponse.class)))
    })
    EventResponseDto getEvent(@PathVariable Long id);
}
