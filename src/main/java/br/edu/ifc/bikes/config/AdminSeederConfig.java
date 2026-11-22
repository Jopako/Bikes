package br.edu.ifc.bikes.config;

import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cria o administrador inicial a partir das propriedades app.admin.*.
 * Como o cadastro publico (POST /api/v1/usuarios) sempre cria CLIENTE,
 * este e o ponto de entrada para obter um ADMIN sem escalonamento de privilegio.
 */
@Configuration
@RequiredArgsConstructor
public class AdminSeederConfig {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String username;

    @Value("${app.admin.password}")
    private String password;

    @Bean
    ApplicationRunner criarAdminInicial() {
        return args -> {
            if (username != null && !username.isBlank()
                    && usuarioRepository.findByUsername(username).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole(Usuario.Role.ADMIN);
                usuarioRepository.save(admin);
            }
        };
    }
}
