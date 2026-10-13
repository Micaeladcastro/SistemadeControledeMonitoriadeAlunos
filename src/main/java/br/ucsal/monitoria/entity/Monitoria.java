package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "monitoria")
@Getter
@Setter
@NoArgsConstructor
public class Monitoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discente_id")
    private Discente discente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    /** Ex.: 2026.1 ou 2026.2. Calculado a partir da data inicial. */
    private String semestre;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "monitoria_dia_semana", joinColumns = @JoinColumn(name = "monitoria_id"))
    @Column(name = "dia_semana")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> diasSemana = new HashSet<>();

    private LocalTime horaInicio;
    private LocalTime horaFim;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;

    /** Monitoria ativa cuja data de encerramento ja passou. */
    public boolean isFinalizada() {
        return dataFim != null && dataFim.isBefore(LocalDate.now());
    }
}
