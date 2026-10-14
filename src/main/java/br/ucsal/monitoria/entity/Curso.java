package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "curso")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sigla;
    private String descricao;
    private String coordenador;

    /** Todo curso pertence obrigatoriamente a um unico CA. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_academico_id")
    private CentroAcademico centroAcademico;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "curso_turno", joinColumns = @JoinColumn(name = "curso_id"))
    @Column(name = "turno")
    @Enumerated(EnumType.STRING)
    private Set<Turno> turnos = new HashSet<>();

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
