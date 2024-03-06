package net.alex.game.queue.config.security;

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
public class TokenAuthentication extends AbstractAuthenticationToken {
    private final PrincipalData principal;
    private final String token;

    public TokenAuthentication(PrincipalData principal,
                               String token,
                               Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
