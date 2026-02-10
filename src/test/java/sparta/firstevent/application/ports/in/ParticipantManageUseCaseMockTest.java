package sparta.firstevent.application.ports.in;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.application.service.ParticipantCommandService;
import sparta.firstevent.domain.event.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantManageUseCaseMockTest {

    @Mock
    EventGetUseCase eventGetUseCase;

    @Mock
    MemberGetUseCase memberGetUseCase;

    @Mock
    ParticipantGetUseCase participantGetUseCase;

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    EventParticipantCountRepository eventParticipantCountRepository;

    @Mock Determinator determinator;

    @InjectMocks
    ParticipantCommandService participantManageUseCase;

    @Spy EventParticipantCount eventParticipantCount;

    @Test
    void applyMock() {
        Long memberId = 1L;
        Long eventId = 1L;
        Long winnerCount = 8L;

        // given
        Event event = EventFixture.registEvent();
        ReflectionTestUtils.setField(event, "id", eventId);
        event.start();
        given(eventGetUseCase.get(eventId)).willReturn(event);

        given (participantGetUseCase.countWinner(eventId)).willReturn(winnerCount);

        given(participantGetUseCase.exists(eventId, memberId)).willReturn(false);

        Participant savedParticipant = Participant.regist(1L, 1L, determinator);
        ReflectionTestUtils.setField(savedParticipant, "id", 1L);
        given(participantRepository.save(any(Participant.class)))
            .willReturn (savedParticipant);

        EventParticipantCount eventParticipantCount = EventParticipantCount.regist(eventId);
        ReflectionTestUtils.setField(eventParticipantCount, "participantCount", 1000);
        ReflectionTestUtils.setField(eventParticipantCount, "winnerCount", winnerCount);

        given(eventParticipantCountRepository.findByEventId(eventId)).willReturn(Optional.of(eventParticipantCount));

        // when
        Participant testParticipant = participantManageUseCase.apply(eventId, memberId);

        // then
        verify(participantRepository, times(1)).save(any(Participant.class));

        assertThat(testParticipant.getId()).isNotNull();
        assertThat(testParticipant.getEventId()).isEqualTo(eventId);
    }

    @Test
    void applySply() {
        Long memberId = 1L;
        Long eventId = 1L;

        Long winnerCount = eventParticipantCount.getParticipantCount();
        Long participantCount = eventParticipantCount.getParticipantCount();

        // given
        Event event = EventFixture.registEvent();
        ReflectionTestUtils.setField(event, "id", eventId);
        event.start();
        given(eventGetUseCase.get(eventId)).willReturn(event);

        given (participantGetUseCase.countWinner(eventId)).willReturn(winnerCount);

        given(participantGetUseCase.exists(eventId, memberId)).willReturn(false);

        Participant savedParticipant = Participant.regist(1L, 1L, determinator);
        ReflectionTestUtils.setField(savedParticipant, "id", 1L);
        given(participantRepository.save(any(Participant.class)))
            .willReturn (savedParticipant);

        given(eventParticipantCountRepository.findByEventId(eventId)).willReturn(Optional.of(eventParticipantCount));

        // when
        Participant testParticipant = participantManageUseCase.apply(eventId, memberId);

        // then
        verify(eventParticipantCountRepository, times(1)).findByEventId(eventId);
        verify(participantRepository, times(1)).save(any(Participant.class));

        verify(eventParticipantCount, times(1)).update();

        assertThat(eventParticipantCount.getParticipantCount()).isEqualTo(participantCount + 1);
        assertThat(eventParticipantCount.getWinnerCount()).isEqualTo(winnerCount);

        assertThat(testParticipant.getId()).isNotNull();
        assertThat(testParticipant.getEventId()).isEqualTo(eventId);


    }

}