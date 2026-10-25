package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class Morador {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String nome;
 @Column(nullable=false) public String email;
 @Column(nullable=false) public String senhaHash;
 public Long republicaId;
}
