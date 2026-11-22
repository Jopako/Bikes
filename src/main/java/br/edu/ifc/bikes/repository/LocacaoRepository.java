package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
    boolean existsByBicicletaAndStatus(Bicicleta bicicleta, Locacao.Status status);
    Optional<Locacao> findByIdAndCliente(Long id, Cliente cliente);
    List<Locacao> findByCliente(Cliente cliente);
    List<Locacao> findByStatus(Locacao.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Locacao l where l.id = :id and l.cliente = :cliente")
    Optional<Locacao> findByIdAndClienteForUpdate(@Param("id") Long id, @Param("cliente") Cliente cliente);
}
