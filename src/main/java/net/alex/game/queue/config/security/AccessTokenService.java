package net.alex.game.queue.config.security;

import jakarta.servlet.http.HttpServletRequest;
import net.alex.game.queue.persistence.entity.TokenEntity;
import net.alex.game.queue.persistence.repo.TokenRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    private static final String AUTH_TOKEN_HEADER_NAME = "Authorization";

    private final TokenRepo tokenRepo;

    public AccessTokenService(TokenRepo tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (StringUtils.isNotBlank(token)) {
            TokenEntity tokenEntity = tokenRepo.findByToken(token);
            if (tokenEntity != null) {
                return new ApiKeyAuthentication(token,
                        tokenEntity.getUser().getRoles().stream().
                                map(r -> (GrantedAuthority) r::getName).toList());
            }
        }
        return null;
    }
}
