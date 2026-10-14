package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "disciplina")
@Getter
@Setter
@NoArgsConstructor
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sigla;
    private String descricao;
    private Integer cargaHoraria;

    /** Toda disciplina pertence obrigatoriamente a um unico curso. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    /** Docente que leciona a disciplina (quem a cadastrou). */
    @ManyToOne(optional = false)
    @JoinColumn(name = "docente_id")
    private Docente docente;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
