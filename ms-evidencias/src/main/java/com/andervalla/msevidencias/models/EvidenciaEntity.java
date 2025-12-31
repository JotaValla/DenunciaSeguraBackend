package com.andervalla.msevidencias.models;

import com.andervalla.msevidencias.models.Enums.EntidadTipoEnum;
import com.andervalla.msevidencias.models.Enums.EstadoEvidenciaEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "evidencias")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Builder
public class EvidenciaEntity {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false, name = "nombre_archivo")
    private String nombreArchivo;

    @Column(nullable = false, name = "ruta_storage")
    private String pathStorage;

    @Column(nullable = false, name = "tipo_contenido")
    private String contentType;

    @Column(nullable = false, name = "tamanio_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvidenciaEnum estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "tipo_entidad")
    private EntidadTipoEnum tipoEntidad;

    @Column(nullable = true, name = "entidad_id")
    private Long entidadId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "creado_en")
    private Instant creadoEn;

    @Column(nullable = false, name = "actualizado_en")
    @UpdateTimestamp
    private Instant actualizadoEn;

    @Column(nullable = false, name = "usuario_creador_id")
    private Long usuarioCreadorId;

}
