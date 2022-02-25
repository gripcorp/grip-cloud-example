package show.grip.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GripErrorResponse {
    private String errorCode;
    private String error;
    private String message;
    private int status;
    private long timestamp;
    private String path;
}
