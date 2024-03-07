package org.fundaciobit.pluginsib.scanweb.springboottester.config;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 
 * @author anadal
 *
 */
@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(ServletRequestBindingException.class)
    public final ResponseEntity<String> handleHeaderException(Exception ex, WebRequest request) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String stackTraceOfException = sw.toString().replace("\n", "\n<br/>");
        String html = "<html><head><title>Error</title></head><body><h1>Error</h1><p><b>" + ex.getMessage() + "</b></p>"
                + stackTraceOfException + "</body></html>";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_HTML).body(html);

    }
    
    @ExceptionHandler({NoHandlerFoundException.class})
    public ResponseEntity<String> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest httpServletRequest) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String stackTraceOfException = sw.toString().replace("\n", "\n<br/>");;
        String html = "<html><head><title>Error</title></head><body><h1>Error</h1><p><b>" + ex.getMessage() + "</b></p>"
                + stackTraceOfException + "</body></html>";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(html);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        /*
        List<String> details = new ArrayList<>();
        details.add(ex.getMessage());
        ErrorResponse error = new ErrorResponse("Server Error", details);
        */
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String stackTraceOfException = sw.toString().replace("\n", "\n<br/>");;
        String html = "<html><head><title>Error</title></head><body><h1>Error</h1><p>" + ex.getMessage() + "</p>"
                + stackTraceOfException + "</body></html>";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_HTML).body(html);
    }
}
