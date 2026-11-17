package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.DevolucaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.dto.RelatorioLocacaoDTO;
import br.edu.ifc.bikes.service.LocacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Locações", description = "Operações de locação, devolução e relatório.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/locacoes")
@RequiredArgsConstructor
public class LocacaoController {

    private final LocacaoService locacaoService;

    @Operation(summary = "Solicitar/efetivar locação de bicicleta (CLIENTE)")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<LocacaoResponseDTO> solicitar(@Valid @RequestBody LocacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.solicitar(dto));
    }

    @Operation(summary = "Registrar devolução da bicicleta (CLIENTE)")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/{id}/devolucao")
    public ResponseEntity<LocacaoResponseDTO> devolver(@PathVariable Long id,
                                                       @RequestBody(required = false) DevolucaoRequestDTO dto) {
        return ResponseEntity.ok(locacaoService.devolver(id, dto));
    }

    @Operation(summary = "Listar minhas locações (CLIENTE)")
    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/minhas")
    public ResponseEntity<List<LocacaoResponseDTO>> minhas() {
        return ResponseEntity.ok(locacaoService.minhasLocacoes());
    }

    @Operation(summary = "Relatório de locações e devoluções (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/relatorio")
    public ResponseEntity<List<RelatorioLocacaoDTO>> relatorio() {
        return ResponseEntity.ok(locacaoService.relatorio());
    }
}
