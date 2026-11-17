package br.edu.ifc.bikes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "bicicletas")
public class Bicicleta extends Auditable implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "marca", nullable = false, length = 100)
    private String marca;

    @Column(name = "cor", length = 50)
    private String cor;

    @Column(name = "ano_fabricacao")
    private Integer anoFabricacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.LIVRE;

    public enum Status {
        LIVRE, OCUPADA, MANUTENCAO
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bicicleta that = (Bicicleta) obj;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
