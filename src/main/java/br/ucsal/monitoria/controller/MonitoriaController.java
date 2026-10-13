package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.MonitoriaDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.MonitoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitorias")
@RequiredArgsConstructor
public class MonitoriaController {

    private final MonitoriaService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MonitoriaDto.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MonitoriaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public MonitoriaDto.Response criar(@Valid @RequestBody MonitoriaDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public MonitoriaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody MonitoriaDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public MonitoriaDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public MonitoriaDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }
}
