package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.NovoPagamento;
import br.ifsp.republica.exception.RegraException;
import br.ifsp.republica.model.Pagamento;
import br.ifsp.republica.model.Republica;
import br.ifsp.republica.repository.PagamentoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PagamentoService {

    private final PagamentoRepository pagamentos;
    private final ContextoRepublica contexto;

    public PagamentoService(PagamentoRepository pagamentos, ContextoRepublica contexto) {
        this.pagamentos = pagamentos;
        this.contexto = contexto;
    }

    @Transactional
    public Pagamento pagar(Long usuarioId, NovoPagamento novoPagamento) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        contexto.exigirMembroDaRepublica(republica, novoPagamento.recebedorId());
        validarRegistroDePagamento(usuarioId, novoPagamento);

        Pagamento pagamento = new Pagamento();
        pagamento.setRepublicaId(republica.getId());
        pagamento.setPagadorId(usuarioId);
        pagamento.setRecebedorId(novoPagamento.recebedorId());
        pagamento.setValor(novoPagamento.valor());
        pagamento.setData(novoPagamento.data());

        return pagamentos.save(pagamento);
    }

    @Transactional
    public Pagamento confirmar(Long usuarioId, Long pagamentoId) {
        Republica republica = contexto.bloquearRepublica(usuarioId);
        Pagamento pagamento = pagamentos.findByIdAndRepublicaId(pagamentoId, republica.getId())
            .orElseThrow(RegraException::ausente);

        if (!pagamento.foiRecebidoPor(usuarioId)) {
            throw RegraException.proibido();
        }

        // O JPA acompanha a entidade carregada e grava a mudança ao concluir a transação.
        pagamento.confirmar();

        return pagamento;
    }

    public List<Pagamento> listarPagamentos(Long usuarioId) {
        Republica republica = contexto.republica(usuarioId);
        return pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republica.getId());
    }

    private void validarRegistroDePagamento(Long usuarioId, NovoPagamento novoPagamento) {
        if (usuarioId.equals(novoPagamento.recebedorId())) {
            throw RegraException.invalido("Pagamento deve ser para outro morador");
        }
        if (novoPagamento.data().isAfter(LocalDate.now())) {
            throw RegraException.invalido("Pagamento não pode ter data futura");
        }
    }
}
