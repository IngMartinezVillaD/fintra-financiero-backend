package co.pluto.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"status", "code", "message", "errors"})
public class BaseErrorResponseDto implements Serializable {
  private String status;
  private Integer code;
  private String message;
  private ArrayList<String> errors;
}
