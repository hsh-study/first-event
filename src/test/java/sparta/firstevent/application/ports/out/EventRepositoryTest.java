package sparta.firstevent.application.ports.out;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void save() {
        Event event = EventFixture.registEvent();

        assertThat(event.getId()).isNull();

        Event savedEvent = eventRepository.save(event);

        assertThat(savedEvent.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Event foundEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();

        assertThat(foundEvent.getId()).isEqualTo(savedEvent.getId());
        assertThat(foundEvent.getTitle()).isEqualTo(savedEvent.getTitle());
        assertThat(foundEvent.getStatus()).isEqualTo(savedEvent.getStatus());
    }

}