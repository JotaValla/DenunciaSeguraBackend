package com.andervalla.msdenuncias.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "denuncia_validacion",
        indexes =  @Index(name = "idx_val_denuncia_id", columnList = "denuncia_id, ocurridoEn")
)
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
@ToString
@Builder
public class DenunciaValidacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false)
    private DenunciaEntity denuncia;

    @Column(nullable = false)
    private boolean aprobada;

    @Column(name = "comentario_observacion", nullable = false, columnDefinition = "text")
    private String comentarioObservacion;

    @Column(nullable = false, name = "validado_por_id")
    private Long validadoPorId;

    @Column(nullable = false, name = "ocurrido_en")
    private Instant ocurridoEn;
}
