package net.alex.game.queue.config.security;

import jakarta.servlet.http.HttpServletRequest;
import net.alex.game.queue.persistence.entity.RoleEntity;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.repo.TokenRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

@Service
public class AccessTokenService {

    public static final String AUTH_TOKEN_HEADER_NAME = "Authorization";

    private final TokenRepo tokenRepo;

    public AccessTokenService(TokenRepo tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public Optional<Authentication> getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        Authentication authentication = null;
        if (StringUtils.isNotBlank(token)) {
            Optional<TokenEntity> tokenEntity = tokenRepo.findByToken(token);
            if (tokenEntity.isPresent() &&
                    tokenEntity.get().getUser().isEnabled() &&
                    tokenEntity.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                authentication = new TokenAuthentication(
                        PrincipalData.fromUserEntity(tokenEntity.get().getUser()),
                        token,
                        tokenEntity.get().getUser().getRoles().stream().map(getAuthorityFunction()).toList());
            }
        }
        return Optional.ofNullable(authentication);
    }

    private Function<RoleEntity, GrantedAuthority> getAuthorityFunction() {
        return r -> (GrantedAuthority) () -> {
            if (r.getResourceId() != null) {
                return r.getName() + ":" + r.getResourceId();
            } else {
                return r.getName();
            }
        };
    }
}
