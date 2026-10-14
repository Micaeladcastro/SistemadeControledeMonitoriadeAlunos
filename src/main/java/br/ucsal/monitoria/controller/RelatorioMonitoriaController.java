package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.RelatorioMonitoriaDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.RelatorioMonitoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios-monitoria")
@RequiredArgsConstructor
public class RelatorioMonitoriaController {

    private final RelatorioMonitoriaService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RelatorioMonitoriaDto.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RelatorioMonitoriaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/por-monitoria/{monitoriaId}")
    @PreAuthorize("isAuthenticated()")
    public RelatorioMonitoriaDto.Response buscarPorMonitoria(@PathVariable Long monitoriaId) {
        return service.buscarPorMonitoria(monitoriaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','USUARIO')")
    public RelatorioMonitoriaDto.Response criar(@Valid @RequestBody RelatorioMonitoriaDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','USUARIO')")
    public RelatorioMonitoriaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody RelatorioMonitoriaDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','USUARIO')")
    public RelatorioMonitoriaDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','USUARIO')")
    public RelatorioMonitoriaDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }
}
