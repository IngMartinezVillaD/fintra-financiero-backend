package co.pluto.utils.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

  private final HttpStatus status;

  public CustomException(String message) {
    super(message);
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public CustomException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public CustomException(String message, HttpStatus status, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public CustomException(Throwable cause) {
    super(cause);
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
