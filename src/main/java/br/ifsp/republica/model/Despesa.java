package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"modeloId","competencia"}))
public class Despesa {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long republicaId;
 public Long autorId;
 public Long pagadorId;
 public String descricao;
 public String categoria;
 @Column(precision=19,scale=2) public BigDecimal valor;
 public LocalDate data;
 public String divisao;
 public Long modeloId;
 public String competencia;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="despesa_parcelas",joinColumns=@JoinColumn(name="despesa_id"))
 @MapKeyColumn(name="morador_id") @Column(name="valor",precision=19,scale=2)
 public Map<Long,BigDecimal> parcelas=new LinkedHashMap<>();
}
