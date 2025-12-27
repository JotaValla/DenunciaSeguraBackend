package com.andervalla.msdenuncias.models;

import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "denuncia_estado_historial",
        indexes = @Index(name = "idx_estado_denuncia_id", columnList = "denuncia_id, ocurridoEn")

)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "denuncia")
@Builder
public class DenunciaEstadoHistorialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false)
    private DenunciaEntity denuncia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30, nullable = true)
    private EstadoDenunciaEnum estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", length = 30)
    private EstadoDenunciaEnum estadoAtual;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private Instant ocurridoEn;

}
