package co.fintra.financiero.services.interfaces;

import co.fintra.financiero.dto.request.LoginRequestDto;
import co.fintra.financiero.dto.request.RefreshRequestDto;
import co.fintra.financiero.dto.response.AuthResponseDto;

public interface IAuthService {
  AuthResponseDto login(LoginRequestDto request);
  AuthResponseDto refresh(RefreshRequestDto request);
}
