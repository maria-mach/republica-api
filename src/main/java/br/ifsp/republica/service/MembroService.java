package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.Convite;
import br.ifsp.republica.dto.Contratos.NovaRepublica;
import br.ifsp.republica.dto.Contratos.Usuario;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.MoradorRepository;
import br.ifsp.republica.repository.PagamentoRepository;
import br.ifsp.republica.repository.RepublicaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MembroService {

    private final MoradorRepository moradores;
    private final RepublicaRepository republicas;
    private final PagamentoRepository pagamentos;
    private final SaldoService saldoService;
    private final ContextoRepublica contexto;

    public MembroService(
            MoradorRepository moradores,
            RepublicaRepository republicas,
            PagamentoRepository pagamentos,
            SaldoService saldoService,
            ContextoRepublica contexto) {
        this.moradores = moradores;
        this.republicas = republicas;
        this.pagamentos = pagamentos;
        this.saldoService = saldoService;
        this.contexto = contexto;
    }

    public Morador buscarMorador(Long moradorId) {
        return contexto.morador(moradorId);
    }

    public Republica buscarRepublica(Long usuarioId) {
        return contexto.republica(usuarioId);
    }

    public List<Usuario> listarMembros(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);

        return moradores.findByRepublicaIdOrderById(republica.getId()).stream()
            .map(Usuario::from)
            .toList();
    }

    @Transactional
    public Republica criarRepublica(Long usuarioId, NovaRepublica novaRepublica) {
        Morador morador = contexto.bloquearMorador(usuarioId);
        contexto.exigirMoradorSemRepublica(morador);

        Republica republica = new Republica();
        republica.setNome(novaRepublica.nome().strip());
        republica.setAdministradorId(usuarioId);
        republica.setCodigoConvite(UUID.randomUUID().toString());

        republicas.saveAndFlush(republica);
        morador.setRepublicaId(republica.getId());

        return republica;
    }

    @Transactional
    public Republica entrarNaRepublica(Long usuarioId, Convite convite) {
        Republica republicaEncontrada = republicas.findByCodigoConvite(convite.codigo())
            .orElseThrow(RegraException::ausente);

        Republica republica = republicas.lockById(republicaEncontrada.getId())
            .orElseThrow(RegraException::ausente);
        Morador morador = contexto.bloquearMorador(usuarioId);
        contexto.exigirMoradorSemRepublica(morador);

        morador.setRepublicaId(republica.getId());

        return republica;
    }

    @Transactional
    public Republica transferirAdministracao(Long usuarioId, Long novoAdministradorId) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirAdministrador(republica, usuarioId);
        contexto.exigirMembroDaRepublica(republica, novoAdministradorId);

        republica.setAdministradorId(novoAdministradorId);

        return republica;
    }

    // O vínculo é removido, mas o morador e seu histórico financeiro são preservados.
    @Transactional
    public void sair(Long usuarioId, Long moradorId) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        if (!usuarioId.equals(moradorId)) {
            contexto.exigirAdministrador(republica, usuarioId);
        }

        contexto.exigirMembroDaRepublica(republica, moradorId);
        validarSaida(republica, moradorId);

        Morador morador = contexto.bloquearMorador(moradorId);
        morador.setRepublicaId(null);
    }

    private void validarSaida(Republica republica, Long moradorId) {
        if (republica.administradaPor(moradorId)) {
            throw RegraException.invalido("Transfira a administração antes de sair");
        }
        if (possuiSaldoPendente(republica, moradorId)) {
            throw RegraException.invalido("Só é possível sair com saldo zero");
        }
        if (possuiPagamentoNaoConfirmado(republica, moradorId)) {
            throw RegraException.invalido("Confirme os pagamentos pendentes antes de sair");
        }
    }

    private boolean possuiSaldoPendente(Republica republica, Long moradorId) {
        BigDecimal saldo = saldoService.calcularSaldos(republica.getId())
            .getOrDefault(moradorId, BigDecimal.ZERO);

        return saldo.signum() != 0;
    }

    private boolean possuiPagamentoNaoConfirmado(Republica republica, Long moradorId) {
        return pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republica.getId()).stream()
            .anyMatch(pagamento -> !pagamento.isConfirmado() && pagamento.envolve(moradorId));
    }
}
