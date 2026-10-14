package br.ucsal.monitoria.report;

import br.ucsal.monitoria.dto.MonitoriaDto;
import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Ies;
import br.ucsal.monitoria.entity.Monitoria;
import br.ucsal.monitoria.entity.RelatorioMonitoria;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.entity.Usuario;
import br.ucsal.monitoria.repository.CentroAcademicoRepository;
import br.ucsal.monitoria.repository.CursoRepository;
import br.ucsal.monitoria.repository.DocenteRepository;
import br.ucsal.monitoria.repository.IesRepository;
import br.ucsal.monitoria.repository.MonitoriaRepository;
import br.ucsal.monitoria.repository.RelatorioMonitoriaRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import br.ucsal.monitoria.service.MonitoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Requisitos 5, 9 e 11: relatorios em PDF com identificacao de quem emitiu. */
@Service
@RequiredArgsConstructor
public class RelatorioPdfService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.of("pt", "BR");

    private final IesRepository iesRepository;
    private final CentroAcademicoRepository centroAcademicoRepository;
    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final MonitoriaRepository monitoriaRepository;
    private final RelatorioMonitoriaRepository relatorioRepository;
    private final MonitoriaService monitoriaService;
    private final UsuarioLogado usuarioLogado;

    /** 5.1: IES, Centros Academicos e Cursos com as respectivas vinculacoes. */
    @Transactional(readOnly = true)
    public byte[] estruturaAcademica() {
        PdfBuilder pdf = new PdfBuilder("Relatorio da Estrutura Academica", emissor("Administrador"), false);
        for (Ies ies : iesRepository.findAllByOrderByNomeAsc()) {
            pdf.secao(ies.getSigla() + " - " + ies.getNome() + " [" + ies.getStatus() + "]")
                    .campo("E-mail", ies.getEmail())
                    .campo("Telefone", ies.getTelefone())
                    .campo("Reitor(a)", ies.getReitor())
                    .campo("Cadastrada em", ies.getDataCadastro().format(DATA));
            if (!ies.getProReitorias().isEmpty()) {
                pdf.tabela(new String[]{"Pro-Reitoria", "Nomeado(a)", "Data da nomeacao"},
                        ies.getProReitorias().stream().map(p -> new Object[]{p.getNome(), p.getNomeado(),
                                p.getDataNomeacao() == null ? null : p.getDataNomeacao().format(DATA)}).toList());
            }
            List<Object[]> linhas = new ArrayList<>();
            for (CentroAcademico ca : centroAcademicoRepository.findByIesIdOrderByNomeAsc(ies.getId())) {
                List<Curso> cursos = cursoRepository.findByCentroAcademicoIdOrderByDescricaoAsc(ca.getId());
                if (cursos.isEmpty()) {
                    linhas.add(new Object[]{ca.getSigla() + " - " + ca.getNome(), ca.getCoordenador(), ca.getStatus(), null, null, null, null});
                }
                for (Curso c : cursos) {
                    linhas.add(new Object[]{ca.getSigla() + " - " + ca.getNome(), ca.getCoordenador(), ca.getStatus(),
                            c.getSigla() + " - " + c.getDescricao(), c.getCoordenador(),
                            c.getTurnos().stream().map(Enum::name).sorted().collect(Collectors.joining(", ")), c.getStatus()});
                }
            }
            pdf.tabela(new String[]{"Centro Academico", "Coordenador(a) CA", "Status CA", "Curso", "Coordenador(a) curso", "Turnos", "Status curso"}, linhas);
        }
        return pdf.gerar();
    }

    /** 5.2: Docentes com o CA vinculado, separando ativos de inativos (ou filtrando por status). */
    @Transactional(readOnly = true)
    public byte[] docentes(Status filtro) {
        PdfBuilder pdf = new PdfBuilder("Relatorio de Docentes", emissor("Administrador"), true);
        for (Status status : filtro == null ? List.of(Status.ATIVO, Status.INATIVO) : List.of(filtro)) {
            List<Docente> docentes = docenteRepository.findByStatusOrderByNomeAsc(status);
            pdf.secao("Docentes " + (status == Status.ATIVO ? "ativos" : "inativos") + " (" + docentes.size() + ")");
            pdf.tabela(new String[]{"Matricula", "Nome", "E-mail", "Telefone", "Centro Academico", "IES", "Cadastro"},
                    docentes.stream().map(d -> new Object[]{d.getMatricula(), d.getNome(), d.getEmail(), d.getTelefone(),
                            d.getCentroAcademico().getSigla(), d.getCentroAcademico().getIes().getSigla(),
                            d.getDataCadastro().format(DATA)}).toList());
        }
        return pdf.gerar();
    }

    /** 9.1: Monitorias ativas em curso, finalizadas por semestre e relatorios das monitorias do colaborador. */
    @Transactional(readOnly = true)
    public byte[] monitoriasDoColaborador() {
        Docente docente = usuarioLogado.docente();
        List<Monitoria> monitorias = monitoriaRepository.findByDocenteIdOrderByDataInicioDesc(docente.getId());
        PdfBuilder pdf = new PdfBuilder("Relatorio de Monitorias do Colaborador",
                emissor("Colaborador") + " - Matricula " + docente.getMatricula(), true);

        pdf.secao("Monitorias ativas");
        pdf.tabela(CABECALHO_MONITORIA, monitorias.stream()
                .filter(m -> m.getStatus() == Status.ATIVO && !m.isFinalizada()).map(this::linhaMonitoria).toList());

        pdf.secao("Monitorias finalizadas por semestre");
        Map<String, List<Monitoria>> porSemestre = new TreeMap<>(Comparator.reverseOrder());
        monitorias.stream().filter(m -> m.getStatus() == Status.ATIVO && m.isFinalizada())
                .forEach(m -> porSemestre.computeIfAbsent(m.getSemestre(), k -> new ArrayList<>()).add(m));
        if (porSemestre.isEmpty()) {
            pdf.texto("Nenhuma monitoria finalizada.");
        }
        porSemestre.forEach((semestre, lista) -> {
            pdf.campo("Semestre", semestre);
            pdf.tabela(CABECALHO_MONITORIA, lista.stream().map(this::linhaMonitoria).toList());
        });

        pdf.secao("Monitorias canceladas");
        pdf.tabela(CABECALHO_MONITORIA, monitorias.stream()
                .filter(m -> m.getStatus() == Status.INATIVO).map(this::linhaMonitoria).toList());

        pdf.secao("Relatorios das monitorias");
        pdf.tabela(new String[]{"Monitoria", "Discente", "Disciplina", "Semestre", "Tipo", "Local", "Status relatorio"},
                relatorioRepository.findByMonitoriaDocenteIdOrderByDataCadastroDesc(docente.getId()).stream()
                        .map(r -> new Object[]{"#" + r.getMonitoria().getId(), r.getMonitoria().getDiscente().getNome(),
                                r.getMonitoria().getDisciplina().getSigla(), r.getMonitoria().getSemestre(), r.getTipo(),
                                r.getLocalAtuacao(), r.getStatus()}).toList());
        return pdf.gerar();
    }

    /** 11.1: Relatorio final da monitoria com todos os dados lancados, docente, discente e disciplina. */
    @Transactional(readOnly = true)
    public byte[] relatorioFinal(Long monitoriaId) {
        Monitoria m = monitoriaService.buscarParaVisualizar(monitoriaId);
        PdfBuilder pdf = new PdfBuilder("Relatorio Final da Monitoria", emissor(null), false);

        pdf.secao("Identificacao")
                .campo("Discente (monitor)", m.getDiscente().getNome() + " - Matricula " + m.getDiscente().getMatricula())
                .campo("Curso do discente", m.getDiscente().getCurso().getDescricao())
                .campo("Docente responsavel", m.getDocente().getNome() + " - Matricula " + m.getDocente().getMatricula())
                .campo("Disciplina", m.getDisciplina().getSigla() + " - " + m.getDisciplina().getDescricao())
                .campo("Curso da disciplina", m.getDisciplina().getCurso().getDescricao())
                .campo("Semestre", m.getSemestre())
                .campo("Periodo", m.getDataInicio().format(DATA) + " a " + m.getDataFim().format(DATA))
                .campo("Dias", diasPorExtenso(m))
                .campo("Horario", m.getHoraInicio() + " as " + m.getHoraFim())
                .campo("Situacao", MonitoriaDto.Response.situacao(m));

        RelatorioMonitoria r = relatorioRepository.findByMonitoriaId(monitoriaId).orElse(null);
        if (r == null) {
            pdf.secao("Relatorio").texto("O discente ainda nao cadastrou o relatorio desta monitoria.");
            return pdf.gerar();
        }
        pdf.secao("Dados da monitoria")
                .campo("Tipo de monitoria", r.getTipo() == null ? null : (r.getTipo().name().charAt(0) + r.getTipo().name().substring(1).toLowerCase()))
                .campo("Local de atuacao", r.getLocalAtuacao())
                .campo("Status do relatorio", r.getStatus());
        pdf.secao("Ocorrencias").texto(r.getOcorrencias());
        pdf.secao("Frequencia dos alunos");
        pdf.tabela(new String[]{"Data", "Aluno", "Matricula", "Presenca"}, r.getFrequencia().stream()
                .map(f -> new Object[]{f.getData().format(DATA), f.getAlunoNome(), f.getAlunoMatricula(),
                        f.isPresente() ? "Presente" : "Ausente"}).toList());
        long presencas = r.getFrequencia().stream().filter(f -> f.isPresente()).count();
        pdf.campo("Total de presencas", presencas + " de " + r.getFrequencia().size() + " registros");
        pdf.secao("Consideracoes finais").texto(r.getConsideracoesFinais());
        return pdf.gerar();
    }

    private static final String[] CABECALHO_MONITORIA =
            {"#", "Discente", "Matricula", "Disciplina", "Semestre", "Dias", "Horario", "Periodo"};

    private Object[] linhaMonitoria(Monitoria m) {
        return new Object[]{m.getId(), m.getDiscente().getNome(), m.getDiscente().getMatricula(),
                m.getDisciplina().getSigla(), m.getSemestre(), diasPorExtenso(m),
                m.getHoraInicio() + "-" + m.getHoraFim(),
                m.getDataInicio().format(DATA) + " a " + m.getDataFim().format(DATA)};
    }

    private String diasPorExtenso(Monitoria m) {
        return m.getDiasSemana().stream().sorted(Comparator.comparing(DayOfWeek::getValue))
                .map(d -> d.getDisplayName(TextStyle.SHORT, PT_BR)).collect(Collectors.joining(", "));
    }

    private String emissor(String papel) {
        Usuario u = usuarioLogado.usuario();
        return (papel == null ? "" : papel + " ") + u.getNome() + " (" + u.getLogin() + ")";
    }
}
