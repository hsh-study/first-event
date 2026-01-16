package sparta.firstevent.domain.member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_member_email", columnNames = {"email"})})
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    @NaturalId
    private String email;

    @Column(nullable = false, length = 150)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(nullable = false)
    private LocalDateTime registAt;

    private Member(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        this.email = email;
        this.password = passwordEncoder.encode(password);
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
        this.role = MemberRole.USER;
        registAt = LocalDateTime.now();
    }

    public static Member regist(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        return new Member(email, password, nickname, passwordEncoder);
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
    }
}
