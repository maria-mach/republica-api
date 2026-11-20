package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.Cadastro;
import br.ifsp.republica.dto.Contratos.Login;
import br.ifsp.republica.dto.Contratos.Token;
import br.ifsp.republica.dto.Contratos.Usuario;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.security.ParametrosToken;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String HASH_PARA_USUARIO_AUSENTE =
        "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final MoradorRepository moradores;
    private final PasswordEncoder passwordEncoder;
    private final EmissorDeToken emissorDeToken;

    public AuthService(
            MoradorRepository moradores,
            PasswordEncoder passwordEncoder,
            EmissorDeToken emissorDeToken) {
        this.moradores = moradores;
        this.passwordEncoder = passwordEncoder;
        this.emissorDeToken = emissorDeToken;
    }

    @Transactional
    public Usuario cadastrar(Cadastro cadastro) {
        String email = normalizarEmail(cadastro.email());
        exigirEmailDisponivel(email);
        exigirSenhaDentroDoLimite(cadastro.senha());

        Morador morador = new Morador();
        morador.setNome(cadastro.nome().strip());
        morador.setEmail(email);
        morador.setSenhaHash(passwordEncoder.encode(cadastro.senha()));

        return Usuario.from(moradores.save(morador));
    }

    public Token login(Login credenciais) {
        Optional<Morador> moradorEncontrado = moradores.findByEmail(normalizarEmail(credenciais.email()));

        if (!credenciaisSaoValidas(moradorEncontrado, credenciais.senha())) {
            throw new RegraException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        Morador morador = moradorEncontrado.get();
        String token = emissorDeToken.emitirPara(morador);

        return new Token(
            token,
            ParametrosToken.TIPO,
            ParametrosToken.VALIDADE_SEGUNDOS,
            Usuario.from(morador));
    }

    private boolean credenciaisSaoValidas(Optional<Morador> moradorEncontrado, String senhaInformada) {
        // Também compara um hash quando a conta não existe para reduzir diferenças de tempo.
        String hashParaComparacao = moradorEncontrado
            .map(Morador::getSenhaHash)
            .orElse(HASH_PARA_USUARIO_AUSENTE);

        // Senha acima do limite nunca é aceita; comparar texto vazio evita erro do BCrypt.
        String senhaParaComparacao = excedeLimiteDeBytes(senhaInformada) ? "" : senhaInformada;
        boolean senhaCorreta = passwordEncoder.matches(senhaParaComparacao, hashParaComparacao);

        return moradorEncontrado.isPresent() && senhaCorreta;
    }

    private void exigirEmailDisponivel(String email) {
        if (moradores.findByEmail(email).isPresent()) {
            throw new RegraException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
    }

    private void exigirSenhaDentroDoLimite(String senha) {
        if (excedeLimiteDeBytes(senha)) {
            throw RegraException.invalido(
                "Senha deve ter no máximo " + ParametrosToken.LIMITE_SENHA_BYTES + " bytes em UTF-8");
        }
    }

    private String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private boolean excedeLimiteDeBytes(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > ParametrosToken.LIMITE_SENHA_BYTES;
    }
}
