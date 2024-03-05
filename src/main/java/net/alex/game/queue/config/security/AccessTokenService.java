package net.alex.game.queue.config.security;

import jakarta.servlet.http.HttpServletRequest;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.repo.TokenRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            if (tokenEntity.isPresent()) {
                authentication = new ApiKeyAuthentication(token,
                        tokenEntity.get().getUser().getRoles().stream().
                                map(r -> (GrantedAuthority) r::getName).toList());
            }
        }
        return Optional.ofNullable(authentication);
    }
}
