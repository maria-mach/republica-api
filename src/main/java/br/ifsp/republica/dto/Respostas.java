package br.ifsp.republica.dto;
import br.ifsp.republica.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class Respostas {
 private Respostas() {}
 public record RepublicaDto(Long id,String nome,String codigoConvite,Long administradorId) {
  public static RepublicaDto from(Republica r) { return new RepublicaDto(r.id,r.nome,r.codigoConvite,r.administradorId); }
 }
 public record DespesaDto(Long id,Long republicaId,Long autorId,Long pagadorId,String descricao,String categoria,
   BigDecimal valor,LocalDate data,String divisao,Long modeloId,String competencia,Map<Long,BigDecimal> parcelas) {
  public static DespesaDto from(Despesa d) { return new DespesaDto(d.id,d.republicaId,d.autorId,d.pagadorId,d.descricao,d.categoria,d.valor,d.data,d.divisao,d.modeloId,d.competencia,Map.copyOf(d.parcelas)); }
 }
 public record RecorrenciaDto(Long id,Long republicaId,String descricao,String categoria,BigDecimal valor,Long pagadorId,
   int dia,String divisao,Map<Long,BigDecimal> participantes) {
  public static RecorrenciaDto from(Recorrencia r) { return new RecorrenciaDto(r.id,r.republicaId,r.descricao,r.categoria,r.valor,r.pagadorId,r.dia,r.divisao,Map.copyOf(r.participantes)); }
 }
 public record PagamentoDto(Long id,Long republicaId,Long pagadorId,Long recebedorId,BigDecimal valor,LocalDate data,boolean confirmado) {
  public static PagamentoDto from(Pagamento p) { return new PagamentoDto(p.id,p.republicaId,p.pagadorId,p.recebedorId,p.valor,p.data,p.confirmado); }
 }
}
