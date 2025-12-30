package oss.fosslight.api.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Map<String, Object>> fileParameterMissiongException(HttpServletRequest request, MultipartException e) {
        return responseService.errorResponse(HttpStatus.BAD_REQUEST,
                "A 'file' parameter is mandatory, though its name may differ depending on the API.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Map<String, Object>> constraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        return responseService.errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(CProjectNotAvailableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Map<String, Object>> userNoPermission(HttpServletRequest request, CProjectNotAvailableException e){
        return responseService.errorResponse(HttpStatus.NOT_FOUND,
                "The user does not have edit permissions for Project " + e.getMessage());
    }



    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<Map<String, Object>> userNotFound(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.errorResponse(HttpStatus.UNAUTHORIZED
                , CoCodeManager.getCodeString(CoConstDef.CD_OPEN_API_MESSAGE, CoConstDef.CD_OPEN_API_USER_NOTFOUND_MESSAGE));
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

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "'" + ex.getParameterName()  + "'" + " parameter is missing or misspelled";

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("msg", error);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getMessage();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("msg", error);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
