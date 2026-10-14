package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.dto.UsuarioDto;
import br.ucsal.monitoria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gerenciamento de usuarios e perfis de acesso (um usuario pode acumular perfis). */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioDto.Response> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto.Response criar(@Valid @RequestBody UsuarioDto.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}/perfis")
    public UsuarioDto.Response alterarPerfis(@PathVariable Long id, @Valid @RequestBody UsuarioDto.PerfisRequest req) {
        return service.alterarPerfis(id, req.perfis());
    }

    @PatchMapping("/{id}/ativar")
    public UsuarioDto.Response ativar(@PathVariable Long id) {
        return service.alterarAtivo(id, true);
    }

    @PatchMapping("/{id}/inativar")
    public UsuarioDto.Response inativar(@PathVariable Long id) {
        return service.alterarAtivo(id, false);
    }
}
