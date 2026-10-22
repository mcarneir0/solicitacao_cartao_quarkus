package br.edu.fiap.cartoes.security;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import br.edu.fiap.cartoes.dto.TokenResponse;
import br.edu.fiap.cartoes.entity.Usuario;

import java.time.Instant;

@ApplicationScoped
public class JwtService {
    @ConfigProperty(name="security.jwt.secret") String segredo;
    @ConfigProperty(name="security.jwt.issuer") String emissor;
    @ConfigProperty(name="security.jwt.expiration-seconds") long duracao;

    public TokenResponse emitir(Usuario usuario) {
        long agoraEpoch = Instant.now().getEpochSecond();
        long expiracaoEpoch = agoraEpoch + duracao;

        String accessToken = Jwt.issuer(emissor)
                .subject(usuario.getEmail())
                .upn(usuario.getEmail())
                .issuedAt(agoraEpoch)
                .expiresAt(expiracaoEpoch)
                .claim("usuarioId", usuario.getId())
                .signWithSecret(segredo); // Correção aplicada aqui

        return new TokenResponse(accessToken, "Bearer", duracao, expiracaoEpoch);
    }
}