package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        try {
            Usuario usuario = usuarioMapper.toUsuario(dto);
            usuario.setPassword(passwordEncoder.encode(dto.password()));
            if (dto.role() != null && !dto.role().isBlank()) {
                usuario.setRole(Usuario.Role.valueOf(dto.role().toUpperCase()));
            } else {
                usuario.setRole(Usuario.Role.CLIENTE);
            }
            return usuarioMapper.toResponse(usuarioRepository.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameUniqueViolationException(
                    String.format("O nome do usuário '%s' já existe", dto.username()));
        }
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO getById(Long id) {
        return usuarioMapper.toResponse(findEntityById(id));
    }

    @Transactional
    public void updatePassword(Long id, UsuarioSenhaDTO dto) {
        if (!dto.novaSenha().equals(dto.confirmaSenha())) {
            throw new PasswordInvalidException("A nova senha não confere com a confirmação.");
        }

        Usuario usuario = findEntityById(id);

        if (!matchesPassword(dto.senhaAtual(), usuario.getPassword())) {
            throw new PasswordInvalidException("A senha atual não confere.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.novaSenha()));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getAll() {
        return usuarioMapper.toResponse(usuarioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    public Usuario findEntityById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id)));
    }

    private boolean matchesPassword(String raw, String stored) {
        if (stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"))) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw != null && raw.equals(stored);
    }
}
