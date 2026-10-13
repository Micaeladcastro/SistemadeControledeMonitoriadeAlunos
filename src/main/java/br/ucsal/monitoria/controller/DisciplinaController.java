package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.DisciplinaDto;
import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.service.DisciplinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
@RequiredArgsConstructor
public class DisciplinaController {

    private final DisciplinaService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public List<DisciplinaDto.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DisciplinaDto.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DisciplinaDto.Response criar(@Valid @RequestBody DisciplinaDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DisciplinaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody DisciplinaDto.Request req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DisciplinaDto.Response inativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.INATIVO, req.motivo());
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public DisciplinaDto.Response ativar(@PathVariable Long id, @Valid @RequestBody StatusDto.MotivoRequest req) {
        return service.alterarStatus(id, Status.ATIVO, req.motivo());
    }

    /** So exclui se nao houver historico vinculado; caso contrario retorna 422 pedindo para inativar. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','COLABORADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
