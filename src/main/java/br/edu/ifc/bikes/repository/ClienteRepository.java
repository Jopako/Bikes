package br.edu.ifc.bikes.repository;

import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuario(Usuario usuario);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    Optional<Cliente> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByUsuarioId(Long usuarioId);
}
