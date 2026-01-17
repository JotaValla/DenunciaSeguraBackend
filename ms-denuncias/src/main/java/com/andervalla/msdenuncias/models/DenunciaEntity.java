package com.andervalla.msdenuncias.models;

import com.andervalla.msdenuncias.models.enums.CategoriaDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.EntidadResponsableEnum;
import com.andervalla.msdenuncias.models.enums.EstadoDenunciaEnum;
import com.andervalla.msdenuncias.models.enums.NivelAnonimatoEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "denuncias")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class DenunciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "creado_en")
    private Instant creadoEn;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(name = "evidencias_ids", columnDefinition = "text")
    private String evidenciasIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_denuncia", nullable = false, length = 40 )
    private CategoriaDenunciaEnum categoriaDenunciaEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_responsable", nullable = true, length = 40)
    private EntidadResponsableEnum entidadResponsable;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_denuncia", nullable = false, length = 30)
    private EstadoDenunciaEnum estadoDenunciaEnum;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_anonimato", nullable = false, length = 20)
    private NivelAnonimatoEnum nivelAnonimatoEnum;

    @Column(name = "ciudadano_id")
    private Long ciudadanoId;

    @Column(name = "operador_id")
    private Long operadorId;

    @Column(name = "jefe_id")
    private Long jefeId;

    @Column(name = "comentario_resolucion", columnDefinition = "text")
    private String comentarioResolucion;

    @Column(name = "comentario_observacion", columnDefinition = "text")
    private String comentarioObservacion;

    @Column(name = "actualizado_en")
    @UpdateTimestamp
    private Instant actualizadoEn;

    // Relaciones
    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ocurridoEn ASC")
    private List<DenunciaEstadoHistorialEntity> historialEstados = new ArrayList<>();

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ocurridoEn ASC")
    private List<DenunciaAsignacionEntity>  asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ocurridoEn ASC")
    private List<DenunciaResolucionEntity> resoluciones = new ArrayList<>();

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ocurridoEn ASC")
    private List<DenunciaValidacionEntity> validaciones = new ArrayList<>();

}
