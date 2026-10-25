package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.model.*;
import br.ifsp.republica.repository.*;
import br.ifsp.republica.exception.RegraException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class RepublicaService {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    private final MoradorRepository moradores;
    private final RepublicaRepository republicas;
    private final DespesaRepository despesas;
    private final RecorrenciaRepository recorrencias;
    private final PagamentoRepository pagamentos;
    private final FechamentoRepository fechamentos;
    private final DivisaoService divisao;
    public RepublicaService(MoradorRepository m,RepublicaRepository r,DespesaRepository d,RecorrenciaRepository rc,
                            PagamentoRepository p,FechamentoRepository f,DivisaoService divisao) {
        moradores=m;republicas=r;despesas=d;recorrencias=rc;pagamentos=p;fechamentos=f;this.divisao=divisao;
    }
    public Morador morador(Long id) { return moradores.findById(id).orElseThrow(RegraException::ausente); }
    public Republica republica(Long usuario) {
        Long id=morador(usuario).republicaId;
        if(id==null) throw RegraException.invalido("Entre ou crie uma república primeiro");
        return republicas.findById(id).orElseThrow(RegraException::ausente);
    }
    // Todas as alterações financeiras e de membros usam a mesma trava por república.
    private Republica travar(Long usuario) {
        var r=republica(usuario);
        var locked=republicas.lockById(r.id).orElseThrow(RegraException::ausente);
        entityManager.refresh(locked);
        var m=morador(usuario);entityManager.refresh(m);
        Long atual=m.republicaId;
        if(!locked.id.equals(atual)) throw RegraException.proibido();
        return locked;
    }
    private void admin(Republica r,Long usuario) { if(!r.administradorId.equals(usuario)) throw RegraException.proibido(); }
    private void pertence(Republica r,Long id) {
        if(!r.id.equals(morador(id).republicaId)) throw RegraException.invalido("Todos os envolvidos devem pertencer à sua república");
    }
    public List<Usuario> membros(Long usuario) {
        return moradores.findByRepublicaIdOrderById(republica(usuario).id).stream().map(AuthService::usuario).toList();
    }
    @Transactional public Republica criar(Long usuario,NovaRepublica dto) {
        Morador m=moradores.lockById(usuario).orElseThrow(RegraException::ausente);
        if(m.republicaId!=null) throw RegraException.invalido("Você já pertence a uma república");
        Republica r=new Republica();r.nome=dto.nome().strip();r.administradorId=usuario;r.codigoConvite=UUID.randomUUID().toString();
        republicas.saveAndFlush(r);m.republicaId=r.id;return r;
    }
    @Transactional public Republica entrar(Long usuario,Convite dto) {
        var r=republicas.findByCodigoConvite(dto.codigo()).orElseThrow(RegraException::ausente);
        r=republicas.lockById(r.id).orElseThrow(RegraException::ausente);
        Morador m=moradores.lockById(usuario).orElseThrow(RegraException::ausente);
        if(m.republicaId!=null) throw RegraException.invalido("Você já pertence a uma república");
        m.republicaId=r.id;return r;
    }
    @Transactional public Republica transferir(Long usuario,Long novoAdmin) {
        var r=travar(usuario);admin(r,usuario);pertence(r,novoAdmin);r.administradorId=novoAdmin;return r;
    }
    @Transactional public void sair(Long usuario,Long alvo) {
        var r=travar(usuario);
        if(!usuario.equals(alvo)) admin(r,usuario);
        pertence(r,alvo);
        if(r.administradorId.equals(alvo)) throw RegraException.invalido("Transfira a administração antes de sair");
        if(saldos(r.id).getOrDefault(alvo,BigDecimal.ZERO).signum()!=0) throw RegraException.invalido("Só é possível sair com saldo zero");
        if(pagamentos.findByRepublicaIdOrderByDataAscIdAsc(r.id).stream().anyMatch(p->!p.confirmado && (p.pagadorId.equals(alvo)||p.recebedorId.equals(alvo))))
            throw RegraException.invalido("Confirme os pagamentos pendentes antes de sair");
        moradores.lockById(alvo).orElseThrow(RegraException::ausente).republicaId=null;
    }
    private YearMonth mes(String valor) {
        try { return YearMonth.parse(valor); } catch(Exception e) { throw RegraException.invalido("Mês deve ter formato AAAA-MM"); }
    }
    private void aberto(Long republica,YearMonth mes) {
        if(fechamentos.existsByRepublicaIdAndMes(republica,mes.toString())) throw RegraException.invalido("Mês fechado: despesas não podem ser alteradas");
    }
    private void participantes(Republica r,NovaDespesa dto) {
        pertence(r,dto.pagadorId());
        dto.participantes().forEach(p->pertence(r,p.moradorId()));
    }
    private Despesa preencher(Despesa d,NovaDespesa dto,Republica r) {
        participantes(r,dto);aberto(r.id,YearMonth.from(dto.data()));
        var parcelas=divisao.dividir(dto);
        d.republicaId=r.id;d.descricao=dto.descricao();d.valor=dto.valor();d.data=dto.data();d.categoria=dto.categoria();
        d.pagadorId=dto.pagadorId();d.divisao=dto.divisao();d.parcelas.clear();d.parcelas.putAll(parcelas);return d;
    }
    public List<Despesa> listar(Long usuario,String competencia) {
        var lista=despesas.findByRepublicaIdOrderByDataAscIdAsc(republica(usuario).id);
        if(competencia==null) return lista;
        var mes=mes(competencia);return lista.stream().filter(d->YearMonth.from(d.data).equals(mes)).toList();
    }
    public Despesa buscar(Long usuario,Long id) { return despesas.findByIdAndRepublicaId(id,republica(usuario).id).orElseThrow(RegraException::ausente); }
    @Transactional public Despesa lancar(Long usuario,NovaDespesa dto) {
        var r=travar(usuario);var d=new Despesa();d.autorId=usuario;
        return despesas.save(preencher(d,dto,r));
    }
    private void editavel(Republica r,Long usuario,Despesa d) {
        if(!d.autorId.equals(usuario) && !r.administradorId.equals(usuario)) throw RegraException.proibido();
        aberto(r.id,YearMonth.from(d.data));
        // Impede recriar dívidas de moradores que já saíram com saldo zero.
        pertence(r,d.pagadorId);d.parcelas.keySet().forEach(id->pertence(r,id));
    }
    @Transactional public Despesa editar(Long usuario,Long id,NovaDespesa dto) {
        var r=travar(usuario);var d=buscar(usuario,id);editavel(r,usuario,d);return preencher(d,dto,r);
    }
    @Transactional public void excluir(Long usuario,Long id) {
        var r=travar(usuario);var d=buscar(usuario,id);editavel(r,usuario,d);despesas.delete(d);
    }
    @Transactional public Recorrencia modelo(Long usuario,NovaRecorrencia dto) {
        var r=travar(usuario);admin(r,usuario);var d=dto.despesa();participantes(r,d);divisao.dividir(d);
        Recorrencia rc=new Recorrencia();rc.republicaId=r.id;rc.descricao=d.descricao();rc.categoria=d.categoria();
        rc.valor=d.valor();rc.pagadorId=d.pagadorId();rc.divisao=d.divisao();rc.dia=dto.dia();
        d.participantes().forEach(p->rc.participantes.put(p.moradorId(),p.parte()==null?BigDecimal.ZERO:p.parte()));
        return recorrencias.save(rc);
    }
    public List<Recorrencia> modelos(Long usuario) { return recorrencias.findByRepublicaIdOrderById(republica(usuario).id); }
    @Transactional public void excluirModelo(Long usuario,Long id) {
        var r=travar(usuario);admin(r,usuario);
        recorrencias.delete(recorrencias.findByIdAndRepublicaId(id,r.id).orElseThrow(RegraException::ausente));
    }
    @Transactional public List<Despesa> gerar(Long usuario,String competencia) {
        var r=travar(usuario);admin(r,usuario);var mes=mes(competencia);aberto(r.id,mes);List<Despesa> geradas=new ArrayList<>();
        for(var rc:recorrencias.findByRepublicaIdOrderById(r.id)) {
            if(despesas.existsByModeloIdAndCompetencia(rc.id,mes.toString())) continue;
            var participantes=rc.participantes.entrySet().stream().map(p->new Participante(p.getKey(),p.getValue())).toList();
            var dto=new NovaDespesa(rc.descricao,rc.valor,mes.atDay(Math.min(rc.dia,mes.lengthOfMonth())),rc.categoria,rc.pagadorId,rc.divisao,participantes);
            var d=preencher(new Despesa(),dto,r);d.autorId=usuario;d.modeloId=rc.id;d.competencia=mes.toString();geradas.add(despesas.save(d));
        }
        return geradas;
    }
    @Transactional public void fechar(Long usuario,String competencia) {
        var r=travar(usuario);admin(r,usuario);var mes=mes(competencia);
        if(!fechamentos.existsByRepublicaIdAndMes(r.id,mes.toString())) {
            Fechamento f=new Fechamento();f.republicaId=r.id;f.mes=mes.toString();fechamentos.save(f);
        }
    }
    @Transactional public Pagamento pagar(Long usuario,NovoPagamento dto) {
        var r=travar(usuario);pertence(r,dto.recebedorId());
        if(usuario.equals(dto.recebedorId())) throw RegraException.invalido("Pagamento deve ser para outro morador");
        if(dto.data().isAfter(LocalDate.now())) throw RegraException.invalido("Pagamento não pode ter data futura");
        Pagamento p=new Pagamento();p.republicaId=r.id;p.pagadorId=usuario;p.recebedorId=dto.recebedorId();p.valor=dto.valor();p.data=dto.data();
        return pagamentos.save(p);
    }
    @Transactional public Pagamento confirmar(Long usuario,Long id) {
        var r=travar(usuario);var p=pagamentos.findByIdAndRepublicaId(id,r.id).orElseThrow(RegraException::ausente);
        if(!p.recebedorId.equals(usuario)) throw RegraException.proibido();
        p.confirmado=true;return p;
    }
    public List<Pagamento> pagamentos(Long usuario) { return pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republica(usuario).id); }
    private Map<Long,BigDecimal> saldos(Long republica) {
        Map<Long,BigDecimal> s=new TreeMap<>();
        moradores.findByRepublicaIdOrderById(republica).forEach(m->s.put(m.id,BigDecimal.ZERO.setScale(2)));
        for(var d:despesas.findByRepublicaIdOrderByDataAscIdAsc(republica)) {
            s.merge(d.pagadorId,d.valor,BigDecimal::add);d.parcelas.forEach((id,v)->s.merge(id,v.negate(),BigDecimal::add));
        }
        for(var p:pagamentos.findByRepublicaIdOrderByDataAscIdAsc(republica)) if(p.confirmado) {
            s.merge(p.pagadorId,p.valor,BigDecimal::add);s.merge(p.recebedorId,p.valor.negate(),BigDecimal::add);
        }
        return s;
    }
    public Saldo saldo(Long usuario) {
        var r=republica(usuario);Map<Long,BigDecimal> pares=new TreeMap<>();
        for(var d:despesas.findByRepublicaIdOrderByDataAscIdAsc(r.id)) {
            if(d.pagadorId.equals(usuario)) d.parcelas.forEach((id,v)->{if(!id.equals(usuario))pares.merge(id,v,BigDecimal::add);});
            else if(d.parcelas.containsKey(usuario)) pares.merge(d.pagadorId,d.parcelas.get(usuario).negate(),BigDecimal::add);
        }
        for(var p:pagamentos.findByRepublicaIdOrderByDataAscIdAsc(r.id)) if(p.confirmado) {
            if(p.pagadorId.equals(usuario)) pares.merge(p.recebedorId,p.valor,BigDecimal::add);
            if(p.recebedorId.equals(usuario)) pares.merge(p.pagadorId,p.valor.negate(),BigDecimal::add);
        }
        return new Saldo(saldos(r.id).getOrDefault(usuario,BigDecimal.ZERO),pares.entrySet().stream()
            .map(p->new SaldoOutro(p.getKey(),p.getValue())).toList());
    }
    public List<Transferencia> simplificar(Long usuario) {
        var s=saldos(republica(usuario).id);List<Transferencia> resultado=new ArrayList<>();
        var devedores=s.entrySet().stream().filter(e->e.getValue().signum()<0).toList();
        var credores=s.entrySet().stream().filter(e->e.getValue().signum()>0).toList();
        int i=0,j=0;
        while(i<devedores.size() && j<credores.size()) {
            var d=devedores.get(i);var c=credores.get(j);var valor=d.getValue().negate().min(c.getValue());
            resultado.add(new Transferencia(d.getKey(),c.getKey(),valor));
            d.setValue(d.getValue().add(valor));c.setValue(c.getValue().subtract(valor));
            if(d.getValue().signum()==0)i++;if(c.getValue().signum()==0)j++;
        }
        return resultado;
    }
    public Map<String,BigDecimal> categorias(Long usuario,String competencia) {
        Map<String,BigDecimal> totais=new TreeMap<>();
        listar(usuario,mes(competencia).toString()).forEach(d->totais.merge(d.categoria,d.valor,BigDecimal::add));return totais;
    }
    public Map<String,BigDecimal> mensal(Long usuario) {
        Map<String,BigDecimal> totais=new TreeMap<>();listar(usuario,null).forEach(d->totais.merge(YearMonth.from(d.data).toString(),d.valor,BigDecimal::add));return totais;
    }
    public List<Evento> extrato(Long usuario) {
        var r=republica(usuario);List<Evento> eventos=new ArrayList<>();
        for(var d:despesas.findByRepublicaIdOrderByDataAscIdAsc(r.id)) {
            if(d.pagadorId.equals(usuario))eventos.add(new Evento(d.data,"DESPESA_PAGA",d.id,d.valor));
            if(d.parcelas.containsKey(usuario))eventos.add(new Evento(d.data,"PARCELA",d.id,d.parcelas.get(usuario).negate()));
        }
        for(var p:pagamentos.findByRepublicaIdOrderByDataAscIdAsc(r.id)) {
            if(p.pagadorId.equals(usuario)||p.recebedorId.equals(usuario)) {
                var valor=p.pagadorId.equals(usuario)?p.valor:p.valor.negate();
                eventos.add(new Evento(p.data,p.confirmado?"PAGAMENTO_CONFIRMADO":"PAGAMENTO_PENDENTE",p.id,valor));
            }
        }
        eventos.sort(Comparator.comparing(Evento::data).thenComparing(Evento::referenciaId).thenComparing(Evento::tipo));return eventos;
    }
}
