package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.LogAuditoriaDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.repository.LogAuditoriaRepository;
import br.ucsal.monitoria.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuditoriaController {

    private final LogAuditoriaRepository logRepository;
    private final HistoricoService historicoService;

    /** Requisito 4: logs de auditoria, paginados, do mais recente para o mais antigo. */
    @GetMapping("/auditoria")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<LogAuditoriaDto> logs(@RequestParam(required = false) String usuario,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "dataHora"));
        var pagina = usuario == null || usuario.isBlank() ? logRepository.findAll(pageable)
                : logRepository.findByUsuarioLoginContainingIgnoreCase(usuario, pageable);
        return pagina.map(LogAuditoriaDto::from);
    }

    /**
     * Historico de ativacao/inativacao de um cadastro.
     * entidade: IES, CENTRO_ACADEMICO, CURSO, DOCENTE, DISCENTE, DISCIPLINA, MONITORIA, RELATORIO_MONITORIA
     */
    @GetMapping("/historico/{entidade}/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public List<StatusDto.HistoricoResponse> historico(@PathVariable String entidade, @PathVariable Long id) {
        return historicoService.listar(entidade, id);
    }
}
