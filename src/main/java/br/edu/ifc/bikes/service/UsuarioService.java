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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    // sem @Transactional: o saveAndFlush executa o INSERT na hora,
    // então a violação de unicidade é capturada aqui dentro.
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        try {
            Usuario usuario = usuarioMapper.toUsuario(dto);
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

        if (!usuario.getPassword().equals(dto.senhaAtual())) {
            throw new PasswordInvalidException("A senha atual não confere.");
        }

        // o Hibernate detecta a alteração e faz o UPDATE ao fim da transação
        usuario.setPassword(dto.novaSenha());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getAll() {
        return usuarioMapper.toResponse(usuarioRepository.findAll());
    }

    private Usuario findEntityById(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id)));
    }
}