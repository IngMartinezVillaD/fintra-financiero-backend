package co.fintra.financiero.models.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(schema = "seguridad", name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false, length = 200)
  private String nombre;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(nullable = false)
  private boolean activo;

  @Column(name = "intentos_fallidos", nullable = false)
  private int intentosFallidos;

  @Column(name = "bloqueado_hasta")
  private OffsetDateTime bloqueadoHasta;

  @Column(name = "ultima_conexion")
  private OffsetDateTime ultimaConexion;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      schema = "seguridad",
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private Set<RolEntity> roles;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;

  @Version
  @Column(nullable = false)
  private Long version;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (roles == null) return Set.of();
    return roles.stream()
        .map(r -> new SimpleGrantedAuthority(r.getNombre()))
        .collect(Collectors.toSet());
  }

  @Override public boolean isAccountNonExpired()    { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() {
    return bloqueadoHasta == null || bloqueadoHasta.isBefore(OffsetDateTime.now());
  }

  @Override
  public boolean isEnabled() {
    return activo && deletedAt == null;
  }
}
