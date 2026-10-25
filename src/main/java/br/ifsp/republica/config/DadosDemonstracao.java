package br.ifsp.republica.config;

import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.service.AuthService;
import br.ifsp.republica.service.RepublicaService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Usuários conhecidos somente para a demonstração da atividade. */
@Component
@Profile("demo")
public class DadosDemonstracao implements ApplicationRunner {
    private final MoradorRepository moradores;
    private final AuthService auth;
    private final RepublicaService republicas;

    public DadosDemonstracao(MoradorRepository moradores, AuthService auth, RepublicaService republicas) {
        this.moradores = moradores;
        this.auth = auth;
        this.republicas = republicas;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Não recria os dados nem altera senhas quando o banco já foi inicializado.
        if (moradores.findByEmail("ana@teste.com").isPresent()) {
            return;
        }

        var ana = auth.cadastrar(new Cadastro("Ana", "ana@teste.com", "senha12345"));
        var bia = auth.cadastrar(new Cadastro("Bia", "bia@teste.com", "senha12345"));
        var caio = auth.cadastrar(new Cadastro("Caio", "caio@teste.com", "senha12345"));

        var republica = republicas.criar(ana.id(), new NovaRepublica("República IFSP"));
        var convite = new Convite(republica.codigoConvite);
        republicas.entrar(bia.id(), convite);
        republicas.entrar(caio.id(), convite);
    }
}
