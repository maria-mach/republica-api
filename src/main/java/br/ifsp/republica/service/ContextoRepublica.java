package br.ifsp.republica.service;

import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.repository.RepublicaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

// Usa a transação do serviço chamador para manter a trava até o fim da operação.
@Component
public class ContextoRepublica {

    @PersistenceContext
    private EntityManager entityManager;

    private final MoradorRepository moradores;
    private final RepublicaRepository republicas;

    public ContextoRepublica(MoradorRepository moradores, RepublicaRepository republicas) {
        this.moradores = moradores;
        this.republicas = republicas;
    }

    public Morador morador(Long moradorId) {
        return moradores.findById(moradorId)
            .orElseThrow(RegraException::ausente);
    }

    public Republica republica(Long usuarioId) {
        Long republicaId = morador(usuarioId).getRepublicaId();
        if (republicaId == null) {
            throw RegraException.invalido("Entre ou crie uma república primeiro");
        }

        return republicas.findById(republicaId)
            .orElseThrow(RegraException::ausente);
    }

    // As alterações financeiras e de membros compartilham a mesma trava por república.
    // Os dados podem mudar durante a espera pela trava, por isso são recarregados depois dela.
    public Republica bloquearRepublica(Long usuarioId) {
        Republica republicaEncontrada = republica(usuarioId);
        Republica republicaBloqueada = republicas.lockById(republicaEncontrada.getId())
            .orElseThrow(RegraException::ausente);
        entityManager.refresh(republicaBloqueada);

        Morador morador = morador(usuarioId);
        entityManager.refresh(morador);

        if (!morador.pertenceA(republicaBloqueada)) {
            throw RegraException.proibido();
        }

        return republicaBloqueada;
    }

    public Morador bloquearMorador(Long moradorId) {
        return moradores.lockById(moradorId)
            .orElseThrow(RegraException::ausente);
    }

    public void exigirAdministrador(Republica republica, Long usuarioId) {
        if (!republica.administradaPor(usuarioId)) {
            throw RegraException.proibido();
        }
    }

    public void exigirMembroDaRepublica(Republica republica, Long moradorId) {
        if (!morador(moradorId).pertenceA(republica)) {
            throw RegraException.invalido("Todos os envolvidos devem pertencer à sua república");
        }
    }

    public void exigirMoradorSemRepublica(Morador morador) {
        if (morador.temRepublica()) {
            throw RegraException.invalido("Você já pertence a uma república");
        }
    }
}
