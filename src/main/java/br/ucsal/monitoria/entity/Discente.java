package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "discente")
@Getter
@Setter
@NoArgsConstructor
public class Discente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matricula;
    private String nome;
    private String email;
    private String telefone;

    /** Todo discente pertence obrigatoriamente a um curso. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    /** Login do discente (perfil USUARIO). */
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
