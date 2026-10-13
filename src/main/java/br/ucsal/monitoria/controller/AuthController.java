package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.audit.AuditoriaInterceptor;
import br.ucsal.monitoria.dto.AuthDto;
import br.ucsal.monitoria.security.UsuarioLogado;
import br.ucsal.monitoria.service.AuthService;
import br.ucsal.monitoria.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioLogado usuarioLogado;

    /** Publico. Devolve o token JWT que o front deve enviar em "Authorization: Bearer ...". */
    @PostMapping("/login")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest req, HttpServletRequest request) {
        request.setAttribute(AuditoriaInterceptor.ATRIBUTO_LOGIN, req.login());
        return authService.login(req);
    }

    /** Dados do usuario logado (perfis, docenteId, discenteId) para o front montar o menu. */
    @GetMapping("/me")
    public AuthDto.UsuarioAtual me() {
        return authService.atual(usuarioLogado.usuario());
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody AuthDto.TrocarSenhaRequest req) {
        usuarioService.trocarSenha(req.senhaAtual(), req.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
