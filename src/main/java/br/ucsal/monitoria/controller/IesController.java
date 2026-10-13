package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.IesDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.IesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ies")
@RequiredArgsConstructor
public class IesController {

    private final IesService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<IesDto.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public IesDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public IesDto.Response criar(@Valid @RequestBody IesDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public IesDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody IesDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public IesDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public IesDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }

    /** So exclui se nao houver historico vinculado; caso contrario retorna 422 pedindo para inativar. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
