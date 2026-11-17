package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.service.BicicletaService;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bicicletas", description = "Operações de bicicletas (restritas ao administrador).")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/bicicletas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BicicletaController {

    private final BicicletaService bicicletaService;

    @Operation(summary = "Criar uma nova bicicleta", responses = {
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BicicletaResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Código já cadastrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping
    public ResponseEntity<BicicletaResponseDTO> create(@Valid @RequestBody BicicletaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicletaService.create(dto));
    }

    @Operation(summary = "Recuperar bicicleta pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<BicicletaResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bicicletaService.getById(id));
    }

    @Operation(summary = "Recuperar bicicleta pelo código")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<BicicletaResponseDTO> getByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(bicicletaService.getByCodigo(codigo));
    }

    @Operation(summary = "Listar todas as bicicletas", responses = {
            @ApiResponse(responseCode = "200", description = "Lista de bicicletas",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BicicletaResponseDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<BicicletaResponseDTO>> getAll() {
        return ResponseEntity.ok(bicicletaService.getAll());
    }

    @Operation(summary = "Atualizar uma bicicleta")
    @PutMapping("/{id}")
    public ResponseEntity<BicicletaResponseDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody BicicletaRequestDTO dto) {
        return ResponseEntity.ok(bicicletaService.update(id, dto));
    }

    @Operation(summary = "Remover uma bicicleta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bicicletaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
