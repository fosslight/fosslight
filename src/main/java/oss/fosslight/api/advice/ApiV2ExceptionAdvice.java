package oss.fosslight.api.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import oss.fosslight.api.service.RestResponseService;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@ControllerAdvice(basePackages = {"oss.fosslight.api.controller.v2", "oss.fosslight.api.controller.lite"})
@Order(ExceptionAdviceOrder.VERSION_SPECIFIC)
@Slf4j
public class ApiV2ExceptionAdvice extends ResponseEntityExceptionHandler {
    private final RestResponseService responseService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Map<String, Object>> handleInternalServerError(
            HttpServletRequest request, Exception e) {
        log.error("Unhandled exception [{} {}]", request.getMethod(), request.getRequestURI(), e);
        return responseService.errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE)
        );
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Map<String, Object>> fileParameterMissiongException(HttpServletRequest request, MultipartException e) {
        return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                "A 'file' parameter is mandatory, though its name may differ depending on the API.");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Map<String, Object>> handleRuntimeException(HttpServletRequest request, RuntimeException e) {
        log.error("Runtime exception during file upload", e);
        // Check if this is a file ID generation error
        if (e.getMessage() != null && e.getMessage().contains("Failed to generate new file ID")) {
            return responseService.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return responseService.errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_UNKNOWN_ERROR_MESSAGE)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Map<String, Object>> constraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        return responseService.errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CInvalidProjectTypeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    protected ResponseEntity<Map<String, Object>> handleCInvalidProjectTypeException(HttpServletRequest request, CInvalidProjectTypeException e){
        return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                "Project Type is invalid. " + e.getMessage());
    }

    @ExceptionHandler(CProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Map<String, Object>> handleProjectNotFound(HttpServletRequest request, CProjectNotFoundException e) {
        return responseService.errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(CSupplementNoticeGenerationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    protected ResponseEntity<Map<String, Object>> handleSupplementNoticeGenerationFailure(HttpServletRequest request, CSupplementNoticeGenerationException e) {
        return responseService.errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(CProjectNotAvailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    protected ResponseEntity<Map<String, Object>> userNoPermission(HttpServletRequest request, CProjectNotAvailableException e){
        String message;
        if (e.getPermissionType() == ProjectPermissionType.VIEW) {
            message = "The user does not have view permissions for Project " + e.getMessage();
        } else {
            message = "The user does not have edit permissions for Project " + e.getMessage();
        }
        return responseService.errorResponse(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<Map<String, Object>> userNotFound(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.errorResponse(HttpStatus.UNAUTHORIZED
                , CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE));
    }

    @ExceptionHandler(CUserAuthFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<Map<String, Object>> userAuthFailed(HttpServletRequest request, CUserAuthFailedException e) {
        // Do not expose token state (missing/expired/invalid) to the client.
        String errorCode = (e.getErrorCode() != null) ? e.getErrorCode() : CoConstDef.ERR_TOKEN_INVALID;
        String errorMsg = (e.getMessage() != null) ? e.getMessage() : CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_SIGNIN_FAILED_MESSAGE);

        log.error("Authorization Error: errorCode = {} / errorMsg = {}", errorCode, errorMsg);

        return responseService.errorResponse(HttpStatus.UNAUTHORIZED,
                errorCode,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_SIGNIN_FAILED_MESSAGE));
    }

    @ExceptionHandler(CSigninFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<Map<String, Object>> emailSignInFailed(HttpServletRequest request, CSigninFailedException e) {
        return responseService.errorResponse(HttpStatus.UNAUTHORIZED,
                CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_SIGNIN_FAILED_MESSAGE));

    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getBindingResult().getFieldError().getDefaultMessage();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("msg", error);
        errorResponse.put("message", error);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "'" + ex.getParameterName()  + "'" + " parameter is missing or misspelled";

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("msg", error);
        errorResponse.put("message", error);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getMessage();
        Map<String, Object> errorResponse = new HashMap<>();

        if (ex instanceof MissingRequestHeaderException) {
            MissingRequestHeaderException missingHeaderEx = (MissingRequestHeaderException) ex;

            // 2. 누락된 헤더의 이름이 'Authorization'인지 확인 (대소문자 무시)
            if ("Authorization".equalsIgnoreCase(missingHeaderEx.getHeaderName())) {
                // 로그 기록 (선택 사항)
                log.error("Missing Authorization Header: {}", ex.getMessage());

                // 3. HTTP 401 (Unauthorized) 에러 반환
                errorResponse.put("errorCode", CoConstDef.ERR_TOKEN_INVALID);
                errorResponse.put("msg", "Missing Authorization header.");
                errorResponse.put("message", "Missing Authorization header.");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }
        }

        // 4. 그 외의 파라미터나 다른 헤더 누락인 경우 기존처럼 HTTP 400 (Bad Request) 유지
        errorResponse.put("error", "Bad Request");
        errorResponse.put("msg", error);
        errorResponse.put("message", error);

        log.error("ServletRequestBindingException: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
