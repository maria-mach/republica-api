package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.exception.RegraException;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

@Service
public class AuthService {
    private final MoradorRepository moradores;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    public AuthService(MoradorRepository moradores,PasswordEncoder passwords,JwtEncoder encoder) {
        this.moradores=moradores;this.passwords=passwords;this.encoder=encoder;
    }
    public static Usuario usuario(Morador m) { return new Usuario(m.id,m.nome,m.email,m.republicaId); }
    @Transactional
    public Usuario cadastrar(Cadastro dto) {
        String email=dto.email().strip().toLowerCase(Locale.ROOT);
        if (moradores.findByEmail(email).isPresent()) throw new RegraException(HttpStatus.CONFLICT,"E-mail já cadastrado");
        if(dto.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw RegraException.invalido("Senha deve ter no máximo 72 bytes em UTF-8");
        Morador m=new Morador();m.nome=dto.nome().strip();m.email=email;m.senhaHash=passwords.encode(dto.senha());
        return usuario(moradores.save(m));
    }
    public Token login(Login dto) {
        var m=moradores.findByEmail(dto.email().strip().toLowerCase(Locale.ROOT));
        String hash=m.map(u->u.senhaHash).orElse("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        String senha=dto.senha();
        if(senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72) senha="";
        boolean ok=passwords.matches(senha,hash);
        if(m.isEmpty() || !ok) throw new RegraException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas");
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer("republica-api").subject(m.get().id.toString())
            .issuedAt(now).expiresAt(now.plusSeconds(7200)).build();
        String token=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        return new Token(token,"Bearer",7200,usuario(m.get()));
    }
}
