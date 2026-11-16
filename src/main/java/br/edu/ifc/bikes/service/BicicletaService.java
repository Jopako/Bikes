package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.mapper.BicicletaMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final BicicletaMapper bicicletaMapper;

    @Transactional
    public BicicletaResponseDTO create(BicicletaRequestDTO dto) {
        Bicicleta bicicleta = bicicletaMapper.toBicicleta(dto);
        aplicarStatus(dto, bicicleta);
        return bicicletaMapper.toResponse(bicicletaRepository.save(bicicleta));
    }

    @Transactional(readOnly = true)
    public BicicletaResponseDTO getById(Long id) {
        return bicicletaMapper.toResponse(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<BicicletaResponseDTO> getAll() {
        return bicicletaMapper.toResponse(bicicletaRepository.findAll());
    }

    @Transactional
    public BicicletaResponseDTO update(Long id, BicicletaRequestDTO dto) {
        Bicicleta bicicleta = findEntityById(id);
        bicicletaMapper.updateFromDto(dto, bicicleta);
        aplicarStatus(dto, bicicleta);
        return bicicletaMapper.toResponse(bicicleta);
    }

    @Transactional
    public void delete(Long id) {
        Bicicleta bicicleta = findEntityById(id);
        bicicletaRepository.delete(bicicleta);
    }

    private Bicicleta findEntityById(Long id) {
        return bicicletaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Bicicleta com id=%d não encontrada", id)));
    }

    private void aplicarStatus(BicicletaRequestDTO dto, Bicicleta bicicleta) {
        if (dto.status() != null && !dto.status().isBlank()) {
            bicicleta.setStatus(Bicicleta.Status.valueOf(dto.status().toUpperCase()));
        }
    }
}
