package br.ucsal.monitoria.security;

import br.ucsal.monitoria.exception.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Restricao arquitetural 1.3.2: o back-end so atende o FRONT-END Application.
 *
 * Toda requisicao para /api/** precisa trazer o header X-Frontend-Key com a chave configurada
 * em app.frontend-key. Quem injeta esse header e o proxy do front-end (proxy do Angular em
 * desenvolvimento, ou nginx em producao), entao a chave nao fica exposta no navegador.
 * Requisicoes sem a chave (Postman, curl, outro site...) sao barradas com 403.
 *
 * Isso se soma ao CORS (que so libera a origem do Angular) e ao isolamento de rede no docker-compose.
 */
@Component
public class FrontendOnlyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Frontend-Key";

    private final byte[] chave;
    private final ObjectMapper objectMapper;

    public FrontendOnlyFilter(@Value("${app.frontend-key:}") String chave, ObjectMapper objectMapper) {
        this.chave = chave.getBytes(StandardCharsets.UTF_8);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Desligado quando a chave esta vazia; preflight CORS (OPTIONS) nunca carrega headers customizados
        return chave.length == 0
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recebida = request.getHeader(HEADER);
        if (recebida == null || !MessageDigest.isEqual(chave, recebida.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    ErroResponse.of(403, "Forbidden", "Acesso permitido somente atraves do front-end"));
            return;
        }
        chain.doFilter(request, response);
    }
}
