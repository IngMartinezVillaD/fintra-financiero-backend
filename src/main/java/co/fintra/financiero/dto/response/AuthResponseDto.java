package co.fintra.financiero.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
  private String accessToken;
  private String refreshToken;
  private long expiresIn;
  private String tokenType;
  private String username;
  private String nombre;
  private List<String> roles;
}
