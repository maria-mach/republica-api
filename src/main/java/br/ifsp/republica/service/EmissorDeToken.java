package br.ifsp.republica.service;

import br.ifsp.republica.model.Morador;
import br.ifsp.republica.security.ParametrosToken;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class EmissorDeToken {

    private final JwtEncoder jwtEncoder;

    public EmissorDeToken(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String emitirPara(Morador morador) {
        Instant instanteAtual = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(ParametrosToken.EMISSOR)
            .subject(morador.getId().toString())
            .issuedAt(instanteAtual)
            .expiresAt(instanteAtual.plusSeconds(ParametrosToken.VALIDADE_SEGUNDOS))
            .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

        return jwtEncoder.encode(parametros).getTokenValue();
    }
}
