package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pro_reitoria")
@Getter
@Setter
@NoArgsConstructor
public class ProReitoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ies_id")
    private Ies ies;

    private String nome;
    private String nomeado;
    private LocalDate dataNomeacao;
}
