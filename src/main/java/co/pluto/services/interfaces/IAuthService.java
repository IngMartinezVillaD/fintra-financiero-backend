package co.pluto.services.interfaces;

import co.pluto.dto.request.LoginRequestDto;
import co.pluto.dto.request.RefreshRequestDto;
import co.pluto.dto.response.AuthResponseDto;

public interface IAuthService {
  AuthResponseDto login(LoginRequestDto request);
  AuthResponseDto refresh(RefreshRequestDto request);
}
