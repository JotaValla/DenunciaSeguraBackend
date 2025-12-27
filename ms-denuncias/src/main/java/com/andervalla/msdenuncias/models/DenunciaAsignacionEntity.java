package com.andervalla.msdenuncias.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "denuncia_asignacion",
        indexes = @Index(name = "idx_asig_denuncia_id", columnList = "denuncia_id, ocurridoEn")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "denuncia")
@Builder
public class DenunciaAsignacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false)
    private DenunciaEntity denuncia;

    @Column(nullable = true)
    private Long operadorAnteriorId;

    @Column(nullable = false)
    private Long operadorNuevoId;

    @Column(nullable = false)
    private Long asignadoPorId;

    @Column(nullable = false)
    private Instant ocurridoEn;

}
