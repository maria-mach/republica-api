package br.ifsp.republica.config;

import br.ifsp.republica.dto.Contratos.Cadastro;
import br.ifsp.republica.dto.Contratos.Convite;
import br.ifsp.republica.dto.Contratos.NovaRepublica;
import br.ifsp.republica.dto.Contratos.Usuario;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.service.AuthService;
import br.ifsp.republica.service.MembroService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Usuários conhecidos somente para a demonstração da atividade. */
@Component
@Profile("demo")
public class DadosDemonstracao implements ApplicationRunner {

    private static final String EMAIL_DA_PRIMEIRA_CONTA = "ana@teste.com";
    private static final String SENHA_DE_DEMONSTRACAO = "senha12345";

    private final MoradorRepository moradores;
    private final AuthService authService;
    private final MembroService membroService;

    public DadosDemonstracao(
            MoradorRepository moradores,
            AuthService authService,
            MembroService membroService) {
        this.moradores = moradores;
        this.authService = authService;
        this.membroService = membroService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Não recria os dados nem altera senhas quando o banco já foi inicializado.
        if (moradores.findByEmail(EMAIL_DA_PRIMEIRA_CONTA).isPresent()) {
            return;
        }

        Usuario ana = cadastrarMorador("Ana", EMAIL_DA_PRIMEIRA_CONTA);
        Usuario bia = cadastrarMorador("Bia", "bia@teste.com");
        Usuario caio = cadastrarMorador("Caio", "caio@teste.com");

        Republica republica = membroService.criarRepublica(ana.id(), new NovaRepublica("República IFSP"));
        Convite convite = new Convite(republica.getCodigoConvite());
        membroService.entrarNaRepublica(bia.id(), convite);
        membroService.entrarNaRepublica(caio.id(), convite);
    }

    private Usuario cadastrarMorador(String nome, String email) {
        return authService.cadastrar(new Cadastro(nome, email, SENHA_DE_DEMONSTRACAO));
    }
}
