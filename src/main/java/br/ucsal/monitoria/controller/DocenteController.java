package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.DocenteDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.DocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final DocenteService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<DocenteDto.Response> listar(@RequestParam(required = false) Status status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DocenteDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DocenteDto.Response criar(@Valid @RequestBody DocenteDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DocenteDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody DocenteDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DocenteDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public DocenteDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }
}
