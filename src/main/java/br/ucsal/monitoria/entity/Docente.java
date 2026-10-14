package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "docente")
@Getter
@Setter
@NoArgsConstructor
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricula;
    private String nome;
    private String email;
    private String telefone;

    /** Todo docente pertence obrigatoriamente a um unico CA. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_academico_id")
    private CentroAcademico centroAcademico;

    /** Login do docente (perfil COLABORADOR). */
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
