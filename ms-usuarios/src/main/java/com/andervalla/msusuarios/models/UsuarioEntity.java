package com.andervalla.msusuarios.models;

import com.andervalla.msusuarios.models.enums.EntidadEnum;
import com.andervalla.msusuarios.models.enums.EstadoUsuarioEnum;
import com.andervalla.msusuarios.models.enums.RolEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Entidad persistente de usuario.
 * Mantiene unicidad por cédula y email.
 */
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_cedula", columnNames = "cedula"),
                @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_usuarios_rol", columnList = "rol"),
                @Index(name = "idx_usuarios_entidad", columnList = "entidad")
        }
)
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String cedula;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolEnum rol;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EntidadEnum entidad;

    @Column(name = "alias_publico", length = 80)
    private String aliasPublico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoUsuarioEnum estado = EstadoUsuarioEnum.ACTIVO;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

}
