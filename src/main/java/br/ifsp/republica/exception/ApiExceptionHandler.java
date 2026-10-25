package br.ifsp.republica.exception;

import java.time.Instant;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ResponseEntity<?> erro(HttpStatus status,String message,Object fields,HttpServletRequest req) {
        return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now().toString(),"status",status.value(),
            "message",message,"path",req.getRequestURI(),"fields",fields));
    }
    @ExceptionHandler(RegraException.class)
    ResponseEntity<?> regra(RegraException e,HttpServletRequest r) { return erro(e.status,e.getMessage(),Map.of(),r); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validacao(MethodArgumentNotValidException e,HttpServletRequest r) {
        Map<String,String> fields=new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f->fields.put(f.getField(),f.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST,"Verifique os campos informados",fields,r);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> formato(Exception e,HttpServletRequest r) { return erro(HttpStatus.BAD_REQUEST,"Corpo, data ou parâmetro inválido",Map.of(),r); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflito(Exception e,HttpServletRequest r) { return erro(HttpStatus.CONFLICT,"Registro duplicado ou em conflito",Map.of(),r); }
}
