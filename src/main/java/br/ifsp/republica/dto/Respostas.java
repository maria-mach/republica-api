package br.ifsp.republica.dto;

import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.model.Pagamento;
import br.ifsp.republica.model.Recorrencia;
import br.ifsp.republica.model.Republica;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class Respostas {

    private Respostas() {
    }

    public record RepublicaDto(Long id, String nome, String codigoConvite, Long administradorId) {

        public static RepublicaDto from(Republica republica) {
            return new RepublicaDto(
                republica.getId(),
                republica.getNome(),
                republica.getCodigoConvite(),
                republica.getAdministradorId());
        }
    }

    public record DespesaDto(
            Long id,
            Long republicaId,
            Long autorId,
            Long pagadorId,
            String descricao,
            String categoria,
            BigDecimal valor,
            LocalDate data,
            String divisao,
            Long modeloId,
            String competencia,
            Map<Long, BigDecimal> parcelas) {

        public static DespesaDto from(Despesa despesa) {
            return new DespesaDto(
                despesa.getId(),
                despesa.getRepublicaId(),
                despesa.getAutorId(),
                despesa.getPagadorId(),
                despesa.getDescricao(),
                despesa.getCategoria(),
                despesa.getValor(),
                despesa.getData(),
                despesa.getDivisao(),
                despesa.getModeloId(),
                despesa.getCompetencia(),
                Map.copyOf(despesa.getParcelas()));
        }
    }

    public record RecorrenciaDto(
            Long id,
            Long republicaId,
            String descricao,
            String categoria,
            BigDecimal valor,
            Long pagadorId,
            int dia,
            String divisao,
            Map<Long, BigDecimal> participantes) {

        public static RecorrenciaDto from(Recorrencia recorrencia) {
            return new RecorrenciaDto(
                recorrencia.getId(),
                recorrencia.getRepublicaId(),
                recorrencia.getDescricao(),
                recorrencia.getCategoria(),
                recorrencia.getValor(),
                recorrencia.getPagadorId(),
                recorrencia.getDia(),
                recorrencia.getDivisao(),
                Map.copyOf(recorrencia.getParticipantes()));
        }
    }

    public record PagamentoDto(
            Long id,
            Long republicaId,
            Long pagadorId,
            Long recebedorId,
            BigDecimal valor,
            LocalDate data,
            boolean confirmado) {

        public static PagamentoDto from(Pagamento pagamento) {
            return new PagamentoDto(
                pagamento.getId(),
                pagamento.getRepublicaId(),
                pagamento.getPagadorId(),
                pagamento.getRecebedorId(),
                pagamento.getValor(),
                pagamento.getData(),
                pagamento.isConfirmado());
        }
    }
}
