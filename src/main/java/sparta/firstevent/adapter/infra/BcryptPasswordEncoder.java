package sparta.firstevent.adapter.infra;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import sparta.firstevent.domain.member.PasswordEncoder;

@Component
public class BcryptPasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}
