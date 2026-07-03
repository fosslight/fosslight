package oss.fosslight.api.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;

import java.util.HashMap;
import java.util.Map;

@Service
public class RestResponseService {
    static class ErrorResponse {
        private int status;
        private String msg;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.msg = message;
        }

        // getters and setters
    }

    public ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String str) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("msg", str);
        return new ResponseEntity<>(resultMap, status);
    }

    public <T> ResponseEntity<T> successResponse(T body) {
        return ResponseEntity.ok(body);
    }

    public ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status) {
        String msg = null;
        switch (status) {
            case BAD_REQUEST:
                msg = CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PARAMETER_ERROR_MESSAGE);
                break;
            case PAYLOAD_TOO_LARGE:
                msg = CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_FILE_SIZEOVER_MESSAGE);
                break;
            case INTERNAL_SERVER_ERROR:
                msg = CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE);
                break;
            case FORBIDDEN:
                msg = CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_PERMISSION_ERROR_MESSAGE);
                break;
            case NOT_FOUND:
                msg = CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_NOT_FOUND_MESSAGE);
                break;
            default:
        }

        if (msg == null) {
            return new ResponseEntity<>(status);
        }

        return errorResponse(status, msg);
    }

}
