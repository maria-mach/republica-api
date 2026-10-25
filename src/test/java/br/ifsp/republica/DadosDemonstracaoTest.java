package br.ifsp.republica;

import br.ifsp.republica.config.DadosDemonstracao;
import br.ifsp.republica.dto.Contratos.Login;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.service.AuthService;
import br.ifsp.republica.service.RepublicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:demo-test;DB_CLOSE_DELAY=-1")
@ActiveProfiles({"test", "demo"})
class DadosDemonstracaoTest {
    @Autowired MoradorRepository moradores;
    @Autowired AuthService auth;
    @Autowired RepublicaService republicas;
    @Autowired DadosDemonstracao dados;

    @Test
    void criaUsuariosComLoginERepublicaSemDuplicarAoReiniciar() {
        assertEquals(3, moradores.count());
        var ana = auth.login(new Login("ana@teste.com", "senha12345")).usuario();
        var bia = auth.login(new Login("bia@teste.com", "senha12345")).usuario();
        var caio = auth.login(new Login("caio@teste.com", "senha12345")).usuario();
        assertEquals(ana.republicaId(), bia.republicaId());
        assertEquals(ana.republicaId(), caio.republicaId());
        assertEquals(ana.id(), republicas.republica(ana.id()).administradorId);
        assertEquals(0, republicas.saldo(bia.id()).saldo().signum());

        dados.run(new DefaultApplicationArguments());
        assertEquals(3, moradores.count());
    }
}
