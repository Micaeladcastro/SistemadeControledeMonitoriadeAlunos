package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ies")
@Getter
@Setter
@NoArgsConstructor
public class Ies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sigla;
    private String nome;
    private String email;
    private String telefone;

    /** Apenas 1 reitor por IES: por isso e um campo, e nao uma lista. */
    private String reitor;

    @OneToMany(mappedBy = "ies", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProReitoria> proReitorias = new ArrayList<>();

    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;
}
