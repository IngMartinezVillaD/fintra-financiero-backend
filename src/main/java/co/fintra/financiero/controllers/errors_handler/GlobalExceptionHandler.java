package co.fintra.financiero.controllers.errors_handler;

import co.fintra.financiero.dto.response.ApiResponseDto;
import co.fintra.financiero.dto.response.BaseErrorResponseDto;
import co.fintra.financiero.utils.exception.BusinessException;
import co.fintra.financiero.utils.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String ERROR_STATUS = "error";

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponseDto> handleBusinessException(BusinessException ex) {
    log.info("BusinessException: {}", ex.getMessage());
    return ResponseEntity.ok(ApiResponseDto.builder()
        .code(HttpStatus.OK.value())
        .message(ex.getMessage())
        .data(new ArrayList<>())
        .build());
  }

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseErrorResponseDto> handleCustomException(CustomException ex) {
    HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
    Throwable root = getRootCause(ex);
    log.warn("CustomException: status={}, message={}", status.value(), root.getMessage());
    return ResponseEntity.status(status).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(status.value())
        .message(root.getMessage())
        .errors(new ArrayList<>(List.of(root.getMessage())))
        .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
    var errors = new ArrayList<String>();
    ex.getBindingResult().getFieldErrors()
        .forEach(e -> errors.add(e.getField() + ": " + e.getDefaultMessage()));
    log.warn("Errores de validación: {}", errors);
    return ResponseEntity.badRequest().body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.BAD_REQUEST.value())
        .message("Error de validación en la petición")
        .errors(errors)
        .build());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<BaseErrorResponseDto> handleAuthenticationException(AuthenticationException ex) {
    log.warn("Autenticación fallida: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.UNAUTHORIZED.value())
        .message("Credenciales inválidas")
        .build());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<BaseErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("Acceso denegado: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.FORBIDDEN.value())
        .message("Acceso denegado")
        .build());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<BaseErrorResponseDto> handleNotFound(NoHandlerFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.NOT_FOUND.value())
        .message("Recurso no encontrado: " + ex.getRequestURL())
        .build());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<BaseErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex) {
    String rootMsg = ex.getMostSpecificCause().getMessage();
    log.error("Violación de integridad: {}", rootMsg);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.CONFLICT.value())
        .message("Conflicto de datos")
        .errors(new ArrayList<>(List.of(rootMsg)))
        .build());
  }

  @ExceptionHandler(SQLException.class)
  public ResponseEntity<BaseErrorResponseDto> handleSQLException(SQLException ex) {
    log.error("Error SQL: SQLState={}, code={}, msg={}", ex.getSQLState(), ex.getErrorCode(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.CONFLICT.value())
        .message("Error de base de datos")
        .errors(new ArrayList<>(List.of(ex.getMessage())))
        .build());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BaseErrorResponseDto> handleNotReadable(HttpMessageNotReadableException ex) {
    Throwable root = getRootCause(ex);
    log.warn("JSON mal formado: {}", root.getMessage());
    return ResponseEntity.badRequest().body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.BAD_REQUEST.value())
        .message("Solicitud JSON mal formada")
        .errors(new ArrayList<>(List.of(root.getMessage())))
        .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseErrorResponseDto> handleGeneral(Exception ex) {
    log.error("Error inesperado", ex);
    String msg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
    if (ex.getCause() != null) msg += " -> " + ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseErrorResponseDto.builder()
        .status(ERROR_STATUS)
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message("Error inesperado en la aplicación")
        .errors(new java.util.ArrayList<>(java.util.List.of(msg)))
        .build());
  }

  private static Throwable getRootCause(Throwable e) {
    return e.getCause() != null ? getRootCause(e.getCause()) : e;
  }
}
