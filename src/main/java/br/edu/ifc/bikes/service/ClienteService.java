package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.ClienteAlreadyRegisteredException;
import br.edu.ifc.bikes.exception.CpfUniqueViolationException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final UsuarioService usuarioService;

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO clienteRequestDTO, String username) {
        Usuario usuario = usuarioService.buscarPorNome(username);

        if (clienteRepository.existsByUsuarioId(usuario.getId())) {
            throw new ClienteAlreadyRegisteredException(
                    String.format("O usuário '%s' já possui um cliente cadastrado", username));
        }

        if (clienteRepository.existsByCpf(clienteRequestDTO.cpf())) {
            throw new CpfUniqueViolationException(
                    String.format("O CPF '%s' já está cadastrado", clienteRequestDTO.cpf()));
        }

        Cliente cliente = clienteMapper.toCliente(clienteRequestDTO);
        cliente.setUsuario(usuario);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponseDTO getById(Long id) {
        return clienteMapper.toResponse(buscarPorId(id));
    }

    @Transactional
    public ClienteResponseDTO getByUsuario(String username) {
        Cliente cliente = clienteRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Cliente do usuário '%s' não encontrado", username)));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public List<ClienteResponseDTO> getAll() {
        return clienteMapper.toResponse(clienteRepository.findAll());
    }

    @Transactional
    public ClienteResponseDTO update(Long id, ClienteRequestDTO clienteRequestDTO) {
        Cliente cliente = buscarPorId(id);

        if (clienteRepository.existsByCpfAndIdNot(clienteRequestDTO.cpf(), id)) {
            throw new CpfUniqueViolationException(
                    String.format("O CPF '%s' já está cadastrado", clienteRequestDTO.cpf()));
        }

        cliente.setNome(clienteRequestDTO.nome());
        cliente.setCpf(clienteRequestDTO.cpf());
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void delete(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
    }

    public boolean isOwner(Long id, String username) {
        return clienteRepository.existsByIdAndUsuarioUsername(id, username);
    }

    private Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Cliente com id=%d não encontrado", id)));
    }
}
