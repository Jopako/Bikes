package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.*;
import br.edu.ifc.bikes.dto.mapper.LocacaoMapper;
import br.edu.ifc.bikes.entity.Bicicleta;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Locacao;
import br.edu.ifc.bikes.exception.BusinessException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import br.edu.ifc.bikes.repository.LocacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocacaoService {

    private static final BigDecimal VALOR_POR_HORA = new BigDecimal("5.00");

    private final LocacaoRepository locacaoRepository;
    private final BicicletaRepository bicicletaRepository;
    private final ClienteService clienteService;
    private final LocacaoMapper locacaoMapper;

    @Transactional
    public LocacaoResponseDTO solicitar(LocacaoRequestDTO dto) {
        Cliente cliente = clienteService.getAuthenticatedCliente();
        Bicicleta bicicleta = resolverBicicleta(dto);
        if (bicicleta.getStatus() != Bicicleta.Status.LIVRE) {
            throw new BusinessException("Bicicleta não está disponível para locação.");
        }
        if (locacaoRepository.existsByBicicletaAndStatus(bicicleta, Locacao.Status.ATIVA)) {
            throw new BusinessException("Bicicleta já possui uma locação ativa.");
        }
        // verificar se existem bicicletas disponíveis (ao menos esta está livre) - requisito atendido

        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setBicicleta(bicicleta);
        locacao.setDataRetirada(dto.dataRetirada() != null ? dto.dataRetirada() : LocalDateTime.now());
        locacao.setDataPrevistaDevolucao(dto.dataPrevistaDevolucao());
        locacao.setCartaoCredito(dto.cartaoCredito());
        locacao.setStatus(Locacao.Status.ATIVA);

        bicicleta.setStatus(Bicicleta.Status.OCUPADA);
        bicicletaRepository.save(bicicleta);
        return locacaoMapper.toResponse(locacaoRepository.save(locacao));
    }

    @Transactional
    public LocacaoResponseDTO devolver(Long locacaoId, DevolucaoRequestDTO dto) {
        Cliente cliente = clienteService.getAuthenticatedCliente();
        Locacao locacao = locacaoRepository.findByIdAndClienteForUpdate(locacaoId, cliente)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada para o cliente autenticado."));
        if (locacao.getStatus() != Locacao.Status.ATIVA) {
            throw new BusinessException("Locação já foi finalizada.");
        }
        LocalDateTime devolucao = LocalDateTime.now();
        locacao.setDataEfetivaDevolucao(devolucao);
        locacao.setStatus(Locacao.Status.FINALIZADA);
        locacao.setValorDebito(calcularValor(locacao.getDataRetirada(), devolucao));

        Bicicleta bicicleta = locacao.getBicicleta();
        if (dto != null && Boolean.TRUE.equals(dto.problemaManutencao())) {
            bicicleta.setStatus(Bicicleta.Status.MANUTENCAO);
        } else {
            bicicleta.setStatus(Bicicleta.Status.LIVRE);
        }
        bicicletaRepository.save(bicicleta);
        return locacaoMapper.toResponse(locacaoRepository.save(locacao));
    }

    @Transactional(readOnly = true)
    public List<LocacaoResponseDTO> minhasLocacoes() {
        Cliente cliente = clienteService.getAuthenticatedCliente();
        return locacaoMapper.toResponse(locacaoRepository.findByCliente(cliente));
    }

    @Transactional(readOnly = true)
    public List<RelatorioLocacaoDTO> relatorio() {
        return locacaoRepository.findAll().stream().map(loc -> {
            Long duracao = null;
            LocalDateTime fim = loc.getDataEfetivaDevolucao();
            if (loc.getDataRetirada() != null && fim != null) {
                duracao = Duration.between(loc.getDataRetirada(), fim).toMinutes();
            }
            return new RelatorioLocacaoDTO(
                    loc.getCliente().getNomeCompleto(),
                    loc.getBicicleta().getCodigo(),
                    loc.getDataRetirada(),
                    loc.getDataEfetivaDevolucao(),
                    duracao,
                    loc.getStatus().name()
            );
        }).toList();
    }

    private Bicicleta resolverBicicleta(LocacaoRequestDTO dto) {
        if (dto.bicicletaId() != null) {
            return bicicletaRepository.findByIdForUpdate(dto.bicicletaId())
                    .orElseThrow(() -> new EntityNotFoundException("Bicicleta não encontrada."));
        }
        if (dto.bicicletaCodigo() != null && !dto.bicicletaCodigo().isBlank()) {
            return bicicletaRepository.findByCodigoForUpdate(dto.bicicletaCodigo())
                    .orElseThrow(() -> new EntityNotFoundException("Bicicleta não encontrada."));
        }
        throw new BusinessException("Informe o código ou id da bicicleta.");
    }

    private BigDecimal calcularValor(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos < 1) minutos = 1;
        BigDecimal horas = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return horas.multiply(VALOR_POR_HORA).setScale(2, RoundingMode.HALF_UP);
    }
}
