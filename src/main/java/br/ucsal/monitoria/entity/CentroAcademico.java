package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "centro_academico")
@Getter
@Setter
@NoArgsConstructor
public class CentroAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sigla;
    private String nome;
    private String email;
    private String telefone;

    /** Apenas 1 coordenador por CA. */
    private String coordenador;

    /** Todo CA pertence obrigatoriamente a uma unica IES. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ies_id")
    private Ies ies;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
