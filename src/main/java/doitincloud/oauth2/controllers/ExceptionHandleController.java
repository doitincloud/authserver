package doitincloud.oauth2.controllers;

import doitincloud.commons.Utils;
import doitincloud.rdbcache.exceptions.NotFoundException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.transaction.TransactionSystemException;

import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@ControllerAdvice
public class ExceptionHandleController extends ResponseEntityExceptionHandler {

    public ExceptionHandleController() {
        super();
    }

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(status, ex);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(status, ex);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(status, ex);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(status, ex);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(status, ex);
    }

    /****************************************/

    @ExceptionHandler({TransactionSystemException.class})
    public @ResponseBody ResponseEntity<Object> handleTransactionSystemException(HttpServletRequest req, final Exception ex, WebRequest request) {
        Throwable e = ExceptionUtils.getRootCause(ex);
        if (e instanceof ConstraintViolationException) {
            return handleConstraintViolationException(req, (ConstraintViolationException) e, request);
        }
        if (e instanceof DataIntegrityViolationException) {
            return handleConstraintViolationException(req, (DataIntegrityViolationException) e, request);
        }
        String error = ex.getClass().getSimpleName();
        String message = e.getMessage();
        String path = httpServletRequest.getRequestURI();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, error, message, path);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public @ResponseBody ResponseEntity<Object> handleConstraintViolationException(HttpServletRequest req, final Exception ex, WebRequest request) {
        String error = ex.getClass().getSimpleName();
        ConstraintViolationException vex = (ConstraintViolationException) ex;
        Set<ConstraintViolation<?>> violations = vex.getConstraintViolations();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (ConstraintViolation<?> violation: violations) {
            String className = violation.getRootBeanClass().getSimpleName();
            className = Utils.toUnderscoreSeparatedName(className);
            String propertyName = violation.getPropertyPath().toString();
            propertyName = Utils.toUnderscoreSeparatedName(propertyName);
            String property = className + "." + propertyName;
            map.add(property, violation.getMessage());
        }
        String path = httpServletRequest.getRequestURI();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, error, map, path);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public @ResponseBody ResponseEntity<Object> handleDataIntegrityViolationException(HttpServletRequest req, final Exception ex, WebRequest request) {
        String error = ex.getClass().getSimpleName();
        DataIntegrityViolationException vex = (DataIntegrityViolationException) ex;
        String message = vex.getRootCause().getMessage();
        String path = httpServletRequest.getRequestURI();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, error, message, path);
    }

    @ExceptionHandler({
            UnauthorizedClientException.class,
            UnauthorizedUserException.class,
            BadCredentialsException.class
    })
    public @ResponseBody ResponseEntity<Object> handleUnauthorizedException(HttpServletRequest req, final Exception ex, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            ResourceAccessException.class,
            NotFoundException.class
    })

    public @ResponseBody ResponseEntity<Object> handleNotFoundException(HttpServletRequest req, final Exception ex, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({
            InsufficientAuthenticationException.class,
            UsernameNotFoundException.class,
            AccessDeniedException.class
    })
    public @ResponseBody ResponseEntity<Object> handleForbiddenException(HttpServletRequest req, final Exception ex, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler({
            UnsupportedOperationException.class,
            InvalidGrantException.class
            //MissingRequiredDataException.class
    })
    public @ResponseBody ResponseEntity<Object> handleBadRequestException(HttpServletRequest req, final Exception ex, WebRequest request) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler({ RuntimeException.class, Exception.class})
    public @ResponseBody ResponseEntity<Object> handleAll(HttpServletRequest req, final Exception ex) {
        ex.printStackTrace();
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, Exception ex) {
        String error = ex.getClass().getSimpleName();
        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        String path = httpServletRequest.getRequestURI();
        return buildResponseEntity(status, error, message, path);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String error, String description) {
        return buildResponseEntity(status, error, description, httpServletRequest.getRequestURI());
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String error, String description, String path) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.put("status", status.value());
        if (error != null && error.length() > 0) map.put("error", error);
        if (description != null && description.length() > 0) map.put("message", description);
        if (path != null && path.length() > 0) map.put("path", path);
        return new ResponseEntity<>(map, status);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String error, MultiValueMap<String, String> msgMap, String path) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.put("status", status.value());
        if (error != null && error.length() > 0) map.put("error", error);
        if (map != null && map.size() > 0) map.put("message", msgMap);
        if (path != null && path.length() > 0) map.put("path", path);
        return new ResponseEntity<>(map, status);
    }
}
