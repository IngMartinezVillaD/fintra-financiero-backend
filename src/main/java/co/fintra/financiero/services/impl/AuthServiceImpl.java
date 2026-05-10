package co.fintra.financiero.services.impl;

import co.fintra.financiero.config.JwtProperties;
import co.fintra.financiero.config.security.JwtService;
import co.fintra.financiero.dto.request.LoginRequestDto;
import co.fintra.financiero.dto.request.RefreshRequestDto;
import co.fintra.financiero.dto.response.AuthResponseDto;
import co.fintra.financiero.models.entity.UsuarioEntity;
import co.fintra.financiero.services.interfaces.IAuthService;
import co.fintra.financiero.utils.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final JwtProperties jwtProperties;

  @Override
  public AuthResponseDto login(LoginRequestDto request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    UsuarioEntity usuario = (UsuarioEntity) userDetailsService.loadUserByUsername(request.getUsername());

    return buildResponse(usuario, jwtService.generateAccessToken(usuario), jwtService.generateRefreshToken(usuario));
  }

  @Override
  public AuthResponseDto refresh(RefreshRequestDto request) {
    String username = jwtService.extractUsername(request.getRefreshToken());
    UsuarioEntity usuario = (UsuarioEntity) userDetailsService.loadUserByUsername(username);

    if (!jwtService.isTokenValid(request.getRefreshToken(), usuario)) {
      throw new CustomException("Refresh token inválido o expirado", HttpStatus.UNAUTHORIZED);
    }

    return buildResponse(usuario, jwtService.generateAccessToken(usuario), request.getRefreshToken());
  }

  private AuthResponseDto buildResponse(UsuarioEntity usuario, String accessToken, String refreshToken) {
    return AuthResponseDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtProperties.getAccessTokenExpiration())
        .tokenType("Bearer")
        .username(usuario.getUsername())
        .nombre(usuario.getNombre())
        .roles(usuario.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .build();
  }
}
