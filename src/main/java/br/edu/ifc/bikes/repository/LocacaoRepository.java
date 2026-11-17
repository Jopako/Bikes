package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
    boolean existsByBicicletaAndStatus(Bicicleta bicicleta, Locacao.Status status);
    Optional<Locacao> findByIdAndCliente(Long id, Cliente cliente);
    List<Locacao> findByCliente(Cliente cliente);
    List<Locacao> findByStatus(Locacao.Status status);
}
