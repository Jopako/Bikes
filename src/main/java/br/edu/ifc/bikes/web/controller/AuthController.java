package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.AuthRequestDTO;
import br.edu.ifc.bikes.dto.AuthResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.security.JwtService;
import br.edu.ifc.bikes.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Login e emissão de token JWT.")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Operation(summary = "Autenticar usuário e obter token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        Usuario usuario = usuarioService.findByUsername(dto.username());
        return ResponseEntity.ok(new AuthResponseDTO(token, "Bearer", usuario.getId(), usuario.getUsername(), usuario.getRole().name()));
    }
}
