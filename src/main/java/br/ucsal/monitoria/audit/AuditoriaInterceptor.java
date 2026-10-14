package br.ucsal.monitoria.audit;

import br.ucsal.monitoria.entity.LogAuditoria;
import br.ucsal.monitoria.repository.LogAuditoriaRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Requisito 4.1: registra TODAS as acoes executadas no sistema (quem, o que, quando, resultado).
 * Roda depois de cada requisicao para /api/** (configurado em WebConfig).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaInterceptor implements HandlerInterceptor {

    /** O AuthController preenche este atributo para registrar tentativas de login. */
    public static final String ATRIBUTO_LOGIN = "auditoria.login";

    private final LogAuditoriaRepository repository;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            LogAuditoria log = new LogAuditoria();
            String login = UsuarioLogado.loginAtualOuNull();
            if (login == null && request.getAttribute(ATRIBUTO_LOGIN) instanceof String tentativa) {
                login = tentativa;
            }
            log.setUsuarioLogin(login);
            log.setMetodo(request.getMethod());
            String uri = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            log.setUri(uri.length() > 500 ? uri.substring(0, 500) : uri);
            if (handler instanceof HandlerMethod hm) {
                log.setAcao(hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName());
            }
            log.setStatusHttp(response.getStatus());
            log.setIp(request.getRemoteAddr());
            repository.save(log);
        } catch (Exception e) {
            AuditoriaInterceptor.log.warn("Falha ao gravar log de auditoria", e);
        }
    }
}
