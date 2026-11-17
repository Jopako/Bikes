package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString(exclude = {"cliente", "bicicleta"})
@Entity
@Table(name = "locacoes")
public class Locacao extends Auditable implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bicicleta_id", nullable = false)
    private Bicicleta bicicleta;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDateTime dataPrevistaDevolucao;

    @Column(name = "data_efetiva_devolucao")
    private LocalDateTime dataEfetivaDevolucao;

    @Column(name = "cartao_credito", length = 30)
    private String cartaoCredito;

    @Column(name = "valor_debito")
    private BigDecimal valorDebito;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.ATIVA;

    public enum Status {
        ATIVA, FINALIZADA
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Locacao locacao = (Locacao) obj;
        return id != null && Objects.equals(id, locacao.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
