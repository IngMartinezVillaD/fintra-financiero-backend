package co.pluto.controllers;

import co.pluto.dto.request.LoginRequestDto;
import co.pluto.dto.request.RefreshRequestDto;
import co.pluto.dto.response.ApiResponseDto;
import co.pluto.services.interfaces.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y renovación de tokens JWT")
public class AuthController extends BaseController {

  private final IAuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Autenticar usuario y obtener tokens JWT")
  public ResponseEntity<ApiResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
    return createSuccessResponse(authService.login(request));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Renovar access token usando refresh token")
  public ResponseEntity<ApiResponseDto> refresh(@Valid @RequestBody RefreshRequestDto request) {
    return createSuccessResponse(authService.refresh(request));
  }
}
