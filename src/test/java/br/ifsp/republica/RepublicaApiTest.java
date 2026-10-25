package br.ifsp.republica;
import br.ifsp.republica.repository.*;
import com.jayway.jsonpath.JsonPath;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class RepublicaApiTest {
 @Autowired MockMvc mvc;
 @Autowired MoradorRepository moradores;
 @Autowired RepublicaRepository republicas;
 @Autowired DespesaRepository despesas;
 @Autowired RecorrenciaRepository recorrencias;
 @Autowired PagamentoRepository pagamentos;
 @Autowired FechamentoRepository fechamentos;
 @Autowired DataSource dataSource;
 String ana,bia,caio; long a,b,c;String convite;
 String call(MockHttpServletRequestBuilder request,String token,String body,int status) throws Exception {
  if(token!=null)request.header("Authorization","Bearer "+token);
  if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(body);
  return mvc.perform(request).andExpect(status().is(status)).andReturn().getResponse().getContentAsString();
 }
 String str(String json,String key) { return JsonPath.read(json,"$."+key); }
 long num(String json,String key) { return ((Number)JsonPath.read(json,"$."+key)).longValue(); }
 double value(String json,String key) { return ((Number)JsonPath.read(json,"$."+key)).doubleValue(); }
 String cadastrar(String nome) throws Exception {
  call(post("/auth/cadastro"),null,"{\"nome\":\""+nome+"\",\"email\":\""+nome+"@teste.com\",\"senha\":\"senha12345\"}",201);
  return call(post("/auth/login"),null,"{\"email\":\""+nome+"@teste.com\",\"senha\":\"senha12345\"}",200);
 }
 @BeforeEach void preparar() throws Exception {
  pagamentos.deleteAll();despesas.deleteAll();recorrencias.deleteAll();fechamentos.deleteAll();moradores.deleteAll();republicas.deleteAll();
  var x=cadastrar("Ana");ana=str(x,"token");a=num(x,"usuario.id");
  x=cadastrar("Bia");bia=str(x,"token");b=num(x,"usuario.id");
  x=cadastrar("Caio");caio=str(x,"token");c=num(x,"usuario.id");
  x=call(post("/republicas"),ana,"{\"nome\":\"República IFSP\"}",201);convite=str(x,"codigoConvite");
  call(post("/republicas/entrar"),bia,"{\"codigo\":\""+convite+"\"}",200);
 }
 String despesa(String tipo,String participantes,String valor,String data) {
  return "{\"descricao\":\"Mercado\",\"valor\":"+valor+",\"data\":\""+data+"\",\"categoria\":\"Alimentação\",\"pagadorId\":"+a+",\"divisao\":\""+tipo+"\",\"participantes\":["+participantes+"]}";
 }
 String igual(String valor) { return despesa("IGUAL","{\"moradorId\":"+a+"},{\"moradorId\":"+b+"}",valor,"2026-10-01"); }
 long criar() throws Exception { return num(call(post("/despesas"),ana,igual("100.00"),201),"id"); }
 @Test void bancoIsoladoESenhasProtegidas() throws Exception {
  try(var conn=dataSource.getConnection()){assertTrue(conn.getMetaData().getURL().startsWith("jdbc:h2:mem:republica-test"));}
  assertTrue(moradores.findById(a).orElseThrow().senhaHash.startsWith("$2"));
  var eu=call(get("/moradores/me"),ana,null,200);assertFalse(eu.contains("senha"));
 }
 @Test void autenticacaoValidacaoEEmailUnico() throws Exception {
  call(get("/despesas"),null,null,401);
  call(get("/despesas"),ana+"x",null,401);
  call(post("/auth/cadastro"),null,"{\"nome\":\"A\",\"email\":\"ANA@TESTE.COM\",\"senha\":\"12345678\"}",409);
  call(post("/auth/cadastro"),null,"{\"nome\":\"A\",\"email\":\"invalido\",\"senha\":\"123\"}",400);
  var x=call(post("/auth/login"),null,"{\"email\":\"Ana@teste.com\",\"senha\":\"errada\"}",401);
  var y=call(post("/auth/login"),null,"{\"email\":\"ninguem@teste.com\",\"senha\":\"errada\"}",401);
  assertEquals(str(x,"message"),str(y,"message"));
 }
 @Test void isolamentoEntreRepublicas() throws Exception {
  long d=criar();call(post("/republicas"),caio,"{\"nome\":\"Outra\"}",201);
  call(get("/despesas/"+d),caio,null,404);call(put("/despesas/"+d),caio,igual("90"),404);
  call(delete("/despesas/"+d),caio,null,404);
  assertEquals("[]",call(get("/despesas"),caio,null,200));
 }
 @Test void impedeDuasRepublicasETransferenciaIndevida() throws Exception {
  call(post("/republicas"),bia,"{\"nome\":\"Outra\"}",400);
  call(post("/republicas/entrar"),bia,"{\"codigo\":\""+convite+"\"}",400);
  call(put("/republicas/minha/administrador"),bia,"{\"moradorId\":"+b+"}",403);
  call(put("/republicas/minha/administrador"),ana,"{\"moradorId\":"+b+"}",200);
  call(delete("/republicas/minha/sair"),ana,null,204);
 }
 @Test void pagamentoSoAfetaSaldoAposConfirmacaoERepeticaoNaoDuplica() throws Exception {
  criar();assertEquals(50,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  var x=call(post("/pagamentos"),bia,"{\"recebedorId\":"+a+",\"valor\":50,\"data\":\"2026-10-01\"}",201);long p=num(x,"id");
  assertEquals(-50,value(call(get("/saldos/me"),bia,null,200),"saldo"));
  call(post("/pagamentos/"+p+"/confirmar"),bia,null,403);
  call(post("/pagamentos/"+p+"/confirmar"),ana,null,200);call(post("/pagamentos/"+p+"/confirmar"),ana,null,200);
  assertEquals(0,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  assertEquals(0,value(call(get("/saldos/me"),bia,null,200),"saldo"));
  call(delete("/republicas/minha/sair"),bia,null,204);
 }
 @Test void sairComDividaOuAdministradorFalha() throws Exception {
  criar();call(delete("/republicas/minha/sair"),bia,null,400);call(delete("/republicas/minha/sair"),ana,null,400);
  call(delete("/republicas/minha/moradores/"+b),ana,null,400);
 }
 @Test void somenteAutorOuAdminEdita() throws Exception {
  long d=criar();call(put("/despesas/"+d),bia,igual("90"),403);call(delete("/despesas/"+d),bia,null,403);
  call(put("/despesas/"+d),ana,igual("90"),200);
  assertEquals(45,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  call(delete("/despesas/"+d),ana,null,204);assertEquals(0,despesas.count());
 }
 @Test void fechamentoBloqueiaOrigemDestinoENovasDespesas() throws Exception {
  long d=criar();call(post("/fechamentos/2026-10"),bia,null,403);call(post("/fechamentos/2026-10"),ana,null,204);
  call(put("/despesas/"+d),ana,igual("90"),400);call(delete("/despesas/"+d),ana,null,400);call(post("/despesas"),ana,igual("10"),400);
  var novembro=igual("20").replace("2026-10-01","2026-11-01");long n=num(call(post("/despesas"),ana,novembro,201),"id");
  call(put("/despesas/"+n),ana,igual("20"),400);
 }
 @Test void recorrenciaNaoDuplicaEAjustaDia31() throws Exception {
  call(post("/recorrencias"),bia,"{\"dia\":31,\"despesa\":"+igual("100")+"}",403);
  call(post("/recorrencias"),ana,"{\"dia\":31,\"despesa\":"+igual("100")+"}",201);
  String x=call(post("/recorrencias/gerar/2026-02"),ana,null,200);assertTrue(x.contains("2026-02-28"));
  assertEquals("[]",call(post("/recorrencias/gerar/2026-02"),ana,null,200));assertEquals(1,despesas.count());
  call(post("/fechamentos/2026-03"),ana,null,204);call(post("/recorrencias/gerar/2026-03"),ana,null,400);
 }
 @Test void erroNaoGravaDespesaNemParcelas() throws Exception {
  var invalida=despesa("PERCENTUAL","{\"moradorId\":"+a+",\"parte\":40},{\"moradorId\":"+b+",\"parte\":40}","100","2026-10-01");
  call(post("/despesas"),ana,invalida,400);assertEquals(0,despesas.count());
  long d=criar();call(put("/despesas/"+d),ana,invalida,400);
  assertEquals(50,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  call(post("/despesas"),ana,igual("-1"),400);
  call(post("/despesas"),ana,igual("1.001"),400);
 }
 @Test void relatoriosESimplificacaoConservamTotal() throws Exception {
  call(post("/republicas/entrar"),caio,"{\"codigo\":\""+convite+"\"}",200);
  var ps="{\"moradorId\":"+a+"},{\"moradorId\":"+b+"},{\"moradorId\":"+c+"}";
  call(post("/despesas"),ana,despesa("IGUAL",ps,"100.00","2026-10-01"),201);
  assertEquals(66.66,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  String simplificado=call(get("/saldos/simplificar"),ana,null,200);
  java.util.List<java.util.Map<String,Object>> lista=JsonPath.read(simplificado,"$");assertEquals(2,lista.size());
  var tokens=java.util.Map.of(a,ana,b,bia,c,caio);
  for(var t:lista) {
   long de=((Number)t.get("de")).longValue(),para=((Number)t.get("para")).longValue();
   String body="{\"recebedorId\":"+para+",\"valor\":"+t.get("valor")+",\"data\":\"2026-10-01\"}";
   long pagamento=num(call(post("/pagamentos"),tokens.get(de),body,201),"id");
   call(post("/pagamentos/"+pagamento+"/confirmar"),tokens.get(para),null,200);
  }
  for(String token:tokens.values()) assertEquals(0,value(call(get("/saldos/me"),token,null,200),"saldo"));
  assertEquals(100,value(call(get("/relatorios/categorias?mes=2026-10"),bia,null,200),"Alimentação"));
  assertTrue(call(get("/relatorios/mensal"),ana,null,200).contains("2026-10"));
  var extrato=call(get("/relatorios/extrato"),ana,null,200);assertTrue(extrato.contains("DESPESA_PAGA"));assertTrue(extrato.contains("PARCELA"));
  call(get("/relatorios/categorias?mes=errado"),ana,null,400);
 }
 @Test void pendenciaImpedeSaidaMesmoComSaldoZero() throws Exception {
  call(post("/pagamentos"),bia,"{\"recebedorId\":"+a+",\"valor\":10,\"data\":\"2026-10-01\"}",201);
  call(delete("/republicas/minha/sair"),bia,null,400);
 }

 @Test void permiteAutorEditarEAdministradorExcluirDespesaDeOutro() throws Exception {
  long d=num(call(post("/despesas"),bia,igual("100"),201),"id");
  call(put("/despesas/"+d),bia,igual("80"),200);
  call(delete("/despesas/"+d),ana,null,204);
 }
 @Test void loteRecorrenteFazRollbackSeParticipanteSaiu() throws Exception {
  var rc="{\"dia\":10,\"despesa\":"+igual("100")+"}";
  String apenasAna=despesa("IGUAL","{\"moradorId\":"+a+"}","10","2026-10-01");
  call(post("/recorrencias"),ana,"{\"dia\":10,\"despesa\":"+apenasAna+"}",201);
  long primeiro=num(call(post("/recorrencias"),ana,rc,201),"id");
  call(delete("/republicas/minha/sair"),bia,null,204);
  call(post("/recorrencias/gerar/2026-12"),ana,null,400);assertEquals(0,despesas.count());
  call(delete("/recorrencias/"+primeiro),ana,null,204);
  call(post("/recorrencias/gerar/2026-12"),ana,null,200);assertEquals(1,despesas.count());
 }
 @Test void rejeitaParticipanteDeOutraRepublicaESenhaLongaNoLogin() throws Exception {
  String body=despesa("IGUAL","{\"moradorId\":"+a+"},{\"moradorId\":"+c+"}","10","2026-10-01");
  call(post("/despesas"),ana,body,400);assertEquals(0,despesas.count());
  call(post("/auth/login"),null,"{\"email\":\"Ana@teste.com\",\"senha\":\""+"x".repeat(100)+"\"}",401);
 }
 @Test void documentacaoDisponivelSemToken() throws Exception {
  var api=call(get("/v3/api-docs"),null,null,200);assertTrue(api.contains("bearerAuth"));
  assertTrue(api.contains("/saldos/simplificar"));
  call(get("/swagger-ui/index.html"),null,null,200);
 }
 @Test void permitePagadorForaDaDivisaoECalculaSaldoPorMorador() throws Exception {
  var body=despesa("IGUAL","{\"moradorId\":"+b+"}","90.00","2026-10-01");
  call(post("/despesas"),ana,body,201);
  assertEquals(90,value(call(get("/saldos/me"),ana,null,200),"saldo"));
  var saldoBia=call(get("/saldos/me"),bia,null,200);
  assertEquals(-90,value(saldoBia,"saldo"));
  assertEquals(-90,value(saldoBia,"porMorador[0].saldo"));
 }
}
