package co.fintra.financiero.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
  @NotBlank(message = "El username es requerido")
  private String username;

  @NotBlank(message = "La contraseña es requerida")
  private String password;
}
