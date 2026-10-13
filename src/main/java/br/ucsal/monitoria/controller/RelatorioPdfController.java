package br.ucsal.monitoria.controller;

import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.report.RelatorioPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Relatorios em PDF. O front pode abrir em nova aba ou baixar o arquivo (blob). */
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioPdfController {

    private final RelatorioPdfService service;

    /** 5.1 - IES, CAs e Cursos com as vinculacoes. */
    @GetMapping("/estrutura-academica")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> estruturaAcademica() {
        return pdf("estrutura-academica.pdf", service.estruturaAcademica());
    }

    /** 5.2 - Docentes (status opcional: ATIVO ou INATIVO; sem filtro separa os dois grupos). */
    @GetMapping("/docentes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> docentes(@RequestParam(required = false) Status status) {
        return pdf("docentes.pdf", service.docentes(status));
    }

    /** 9.1 - Monitorias do colaborador logado. */
    @GetMapping("/minhas-monitorias")
    @PreAuthorize("hasRole('COLABORADOR')")
    public ResponseEntity<byte[]> minhasMonitorias() {
        return pdf("monitorias.pdf", service.monitoriasDoColaborador());
    }

    /** 11.1 - Relatorio final de uma monitoria (discente da monitoria, docente responsavel ou administrador). */
    @GetMapping("/monitorias/{monitoriaId}/final")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> relatorioFinal(@PathVariable Long monitoriaId) {
        return pdf("relatorio-monitoria-" + monitoriaId + ".pdf", service.relatorioFinal(monitoriaId));
    }

    private ResponseEntity<byte[]> pdf(String nomeArquivo, byte[] conteudo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nomeArquivo).build().toString())
                .body(conteudo);
    }
}
