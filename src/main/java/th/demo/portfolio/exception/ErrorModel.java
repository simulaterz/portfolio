package th.demo.portfolio.exception;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ErrorModel {
    private final String code;
    private final String message;
}

