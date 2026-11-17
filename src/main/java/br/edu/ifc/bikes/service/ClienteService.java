package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.BusinessException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        Usuario usuario = getAuthenticatedUsuario();
        if (usuario.getRole() != Usuario.Role.CLIENTE) {
            throw new BusinessException("Apenas usuários com perfil CLIENTE podem realizar cadastro de cliente.");
        }
        if (clienteRepository.existsByUsuarioId(usuario.getId())) {
            throw new BusinessException("Usuário já possui cadastro de cliente.");
        }
        try {
            Cliente cliente = clienteMapper.toCliente(dto);
            cliente.setUsuario(usuario);
            return clienteMapper.toResponse(clienteRepository.saveAndFlush(cliente));
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameUniqueViolationException("CPF já cadastrado.");
        }
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO getById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", id)));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> getAll() {
        return clienteMapper.toResponse(clienteRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO getMe() {
        Usuario usuario = getAuthenticatedUsuario();
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cadastro de cliente não encontrado para o usuário autenticado."));
        return clienteMapper.toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente getAuthenticatedCliente() {
        Usuario usuario = getAuthenticatedUsuario();
        return clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cadastro de cliente não encontrado para o usuário autenticado."));
    }

    private Usuario getAuthenticatedUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BusinessException("Usuário não autenticado.");
        }
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado."));
    }
}
