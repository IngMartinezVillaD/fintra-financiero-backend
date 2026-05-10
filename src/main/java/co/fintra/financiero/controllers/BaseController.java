package co.fintra.financiero.controllers;

import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.dto.response.BaseErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class BaseController {

  protected ResponseEntity<ApiResponseDto> createSuccessResponse(Object data) {
    return ResponseEntity.ok(ApiResponseDto.builder()
        .code(HttpStatus.OK.value())
        .message("success")
        .data(data)
        .build());
  }

  protected ResponseEntity<ApiResponseDto> createSuccessResponseList(List<?> data) {
    return ResponseEntity.ok(ApiResponseDto.builder()
        .code(HttpStatus.OK.value())
        .message("success")
        .data(data)
        .build());
  }

  protected ResponseEntity<ApiResponseDto> createCustomResponse(Object data, String message, HttpStatus status) {
    return ResponseEntity.status(status).body(ApiResponseDto.builder()
        .code(status.value())
        .message(message)
        .data(data)
        .build());
  }

  protected ResponseEntity<BaseErrorResponseDto> createErrorResponse(String message, HttpStatus status, ArrayList<String> errors) {
    return ResponseEntity.status(status).body(BaseErrorResponseDto.builder()
        .status(status.name())
        .code(status.value())
        .message(message)
        .errors(errors)
        .build());
  }
}
