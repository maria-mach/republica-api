package br.ifsp.republica;
import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.service.DivisaoService;
import br.ifsp.republica.exception.RegraException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DivisaoServiceTest {
 final DivisaoService service=new DivisaoService();
 NovaDespesa dto(String tipo,String valor,Participante... ps) {
  return new NovaDespesa("Mercado",new BigDecimal(valor),LocalDate.now(),"Casa",1L,tipo,List.of(ps));
 }
 Participante p(long id,String parte) { return new Participante(id,parte==null?null:new BigDecimal(parte)); }
 @Test void igualConservaCentavosNoPagador() {
  var s=service.dividir(dto("IGUAL","100.00",p(1,null),p(2,null),p(3,null)));
  assertEquals(new BigDecimal("33.34"),s.get(1L));assertEquals(new BigDecimal("33.33"),s.get(2L));
  assertEquals(new BigDecimal("100.00"),s.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add));
 }
 @Test void percentualArredondaSemPerderCentavos() {
  var s=service.dividir(dto("PERCENTUAL","0.01",p(1,"50"),p(2,"50")));
  assertEquals(new BigDecimal("0.01"),s.get(1L));assertEquals(new BigDecimal("0.00"),s.get(2L));
 }
 @Test void valorPermiteParteZeroDoPagador() {
  var s=service.dividir(dto("VALOR","10",p(1,"0"),p(2,"10")));
  assertEquals(new BigDecimal("0.00"),s.get(1L));assertEquals(new BigDecimal("10.00"),s.get(2L));
 }
 @Test void rejeitaSomasInvalidasEDuplicados() {
  assertThrows(RegraException.class,()->service.dividir(dto("PERCENTUAL","10",p(1,"40"),p(2,"40"))));
  assertThrows(RegraException.class,()->service.dividir(dto("VALOR","10",p(1,"4"),p(2,"4"))));
  assertThrows(RegraException.class,()->service.dividir(dto("IGUAL","10",p(1,null),p(1,null))));
 }
 @Test void pagadorPodePagarSomentePelosOutros() {
  var parcelas=service.dividir(dto("IGUAL","100.00",p(2,null),p(3,null)));
  assertFalse(parcelas.containsKey(1L));
  assertEquals(new BigDecimal("50.00"),parcelas.get(2L));
  assertEquals(new BigDecimal("50.00"),parcelas.get(3L));
 }
 @Test void restoFicaComPagadorMesmoForaDosParticipantes() {
  var parcelas=service.dividir(dto("IGUAL","100.01",p(2,null),p(3,null)));
  assertEquals(new BigDecimal("0.01"),parcelas.get(1L));
  assertEquals(new BigDecimal("100.01"),parcelas.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add));
 }
}
