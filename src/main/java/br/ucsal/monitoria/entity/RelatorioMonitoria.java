package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Relatorio final preenchido pelo discente (monitor). */
@Entity
@Table(name = "relatorio_monitoria")
@Getter
@Setter
@NoArgsConstructor
public class RelatorioMonitoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "monitoria_id")
    private Monitoria monitoria;

    @Enumerated(EnumType.STRING)
    private TipoMonitoria tipo;

    private String localAtuacao;
    private String ocorrencias;
    private String consideracoesFinais;

    @ElementCollection
    @CollectionTable(name = "relatorio_frequencia", joinColumns = @JoinColumn(name = "relatorio_id"))
    @OrderBy("data ASC")
    private List<RegistroFrequencia> frequencia = new ArrayList<>();

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
