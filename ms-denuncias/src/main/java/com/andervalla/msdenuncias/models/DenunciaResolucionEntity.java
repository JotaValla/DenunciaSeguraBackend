package com.andervalla.msdenuncias.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "denuncia_resolucion",
        indexes = @Index(name = "idx_res_denuncia_id", columnList = "denuncia_id, ocurridoEn")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "denuncia")
@Builder
public class DenunciaResolucionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false)
    private DenunciaEntity denuncia;

    @Column(nullable = false, name = "comentario_resolucion")
    private String comentarioResolucion;

    @Column(nullable = false, columnDefinition = "text")
    private String evienciaIds;

    @Column(nullable = false, name = "resuelto_por_id")
    private Long resueltoPorId;

    @Column(nullable = false)
    private Instant ocurridoEn;

}
