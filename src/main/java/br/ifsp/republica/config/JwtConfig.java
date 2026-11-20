package br.ifsp.republica.config;

import br.ifsp.republica.security.ParametrosToken;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

    private static final String ALGORITMO_CHAVE = "HmacSHA256";
    private static final int TAMANHO_MINIMO_CHAVE_BYTES = 32;

    @Bean
    SecretKey jwtKey(@Value("${app.jwt.secret:}") String segredo) {
        if (segredo.isBlank()) {
            return gerarChaveTemporaria();
        }

        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_CHAVE_BYTES) {
            throw new IllegalArgumentException("JWT_SECRET deve ter pelo menos 32 bytes");
        }

        return new SecretKeySpec(bytes, ALGORITMO_CHAVE);
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey chave) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ParametrosToken.EMISSOR));

        return decoder;
    }

    private SecretKey gerarChaveTemporaria() {
        byte[] bytes = new byte[TAMANHO_MINIMO_CHAVE_BYTES];
        new SecureRandom().nextBytes(bytes);

        return new SecretKeySpec(bytes, ALGORITMO_CHAVE);
    }
}
