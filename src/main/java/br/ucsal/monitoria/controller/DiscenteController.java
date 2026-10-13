package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.DiscenteDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.DiscenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discentes")
@RequiredArgsConstructor
public class DiscenteController {

    private final DiscenteService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public List<DiscenteDto.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DiscenteDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DiscenteDto.Response criar(@Valid @RequestBody DiscenteDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DiscenteDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody DiscenteDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DiscenteDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DiscenteDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }
}
