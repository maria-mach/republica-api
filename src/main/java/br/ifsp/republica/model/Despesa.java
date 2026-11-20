package br.ifsp.republica.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"modeloId", "competencia"}))
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long republicaId;

    private Long autorId;

    private Long pagadorId;

    private String descricao;

    private String categoria;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;

    private LocalDate data;

    private String divisao;

    private Long modeloId;

    private String competencia;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "despesa_parcelas", joinColumns = @JoinColumn(name = "despesa_id"))
    @MapKeyColumn(name = "morador_id")
    @Column(name = "valor", precision = 19, scale = 2)
    private Map<Long, BigDecimal> parcelas = new LinkedHashMap<>();

    public Long getId() {
        return id;
    }

    public Long getRepublicaId() {
        return republicaId;
    }

    public void setRepublicaId(Long republicaId) {
        this.republicaId = republicaId;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public Long getPagadorId() {
        return pagadorId;
    }

    public void setPagadorId(Long pagadorId) {
        this.pagadorId = pagadorId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDivisao() {
        return divisao;
    }

    public void setDivisao(String divisao) {
        this.divisao = divisao;
    }

    public Long getModeloId() {
        return modeloId;
    }

    public void setModeloId(Long modeloId) {
        this.modeloId = modeloId;
    }

    public String getCompetencia() {
        return competencia;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public Map<Long, BigDecimal> getParcelas() {
        return Collections.unmodifiableMap(parcelas);
    }

    // Mantém a coleção gerenciada pelo JPA e substitui seu conteúdo também na edição.
    public void substituirParcelas(Map<Long, BigDecimal> novasParcelas) {
        parcelas.clear();
        parcelas.putAll(novasParcelas);
    }

    public YearMonth getMesDaData() {
        return YearMonth.from(data);
    }

    public boolean foiPagaPor(Long moradorId) {
        return pagadorId.equals(moradorId);
    }

    public boolean temParcelaDe(Long moradorId) {
        return parcelas.containsKey(moradorId);
    }

    public BigDecimal parcelaDe(Long moradorId) {
        return parcelas.get(moradorId);
    }
}
