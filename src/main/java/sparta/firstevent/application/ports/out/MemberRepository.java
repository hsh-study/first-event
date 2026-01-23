package sparta.firstevent.application.ports.out;

import org.springframework.data.repository.Repository;
import sparta.firstevent.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    Optional<Member> findById(Long id);
}
