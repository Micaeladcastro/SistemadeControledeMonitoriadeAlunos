package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.AuthDto;
import br.ucsal.monitoria.entity.Usuario;
import br.ucsal.monitoria.repository.DiscenteRepository;
import br.ucsal.monitoria.repository.DocenteRepository;
import br.ucsal.monitoria.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DocenteRepository docenteRepository;
    private final DiscenteRepository discenteRepository;

    /** Valida login/senha contra o banco (BCrypt) e devolve o token JWT. */
    @Transactional(readOnly = true)
    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.login(), req.senha()));
        Usuario usuario = (Usuario) auth.getPrincipal();
        return new AuthDto.LoginResponse(jwtService.gerarToken(usuario), "Bearer", jwtService.expiracao(), atual(usuario));
    }

    @Transactional(readOnly = true)
    public AuthDto.UsuarioAtual atual(Usuario usuario) {
        Long docenteId = docenteRepository.findByUsuarioLogin(usuario.getLogin()).map(d -> d.getId()).orElse(null);
        Long discenteId = discenteRepository.findByUsuarioLogin(usuario.getLogin()).map(d -> d.getId()).orElse(null);
        return new AuthDto.UsuarioAtual(usuario.getId(), usuario.getLogin(), usuario.getNome(),
                Set.copyOf(usuario.getPerfis()), docenteId, discenteId);
    }
}
