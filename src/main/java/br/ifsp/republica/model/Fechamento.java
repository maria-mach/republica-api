package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"republicaId","mes"}))
public class Fechamento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long republicaId;
 public String mes;
}
