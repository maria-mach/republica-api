package br.ifsp.republica.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final String MENSAGEM_CAMPOS_INVALIDOS = "Verifique os campos informados";
    private static final String MENSAGEM_FORMATO_INVALIDO = "Corpo, data ou parâmetro inválido";
    private static final String MENSAGEM_CONFLITO = "Registro duplicado ou em conflito";

    @ExceptionHandler(RegraException.class)
    ResponseEntity<?> tratarRegra(RegraException excecao, HttpServletRequest requisicao) {
        return montarResposta(excecao.getStatus(), excecao.getMessage(), Map.of(), requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> tratarValidacao(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        Map<String, String> campos = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors()
            .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        return montarResposta(HttpStatus.BAD_REQUEST, MENSAGEM_CAMPOS_INVALIDOS, campos, requisicao);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> tratarFormatoInvalido(Exception excecao, HttpServletRequest requisicao) {
        return montarResposta(HttpStatus.BAD_REQUEST, MENSAGEM_FORMATO_INVALIDO, Map.of(), requisicao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> tratarConflito(Exception excecao, HttpServletRequest requisicao) {
        return montarResposta(HttpStatus.CONFLICT, MENSAGEM_CONFLITO, Map.of(), requisicao);
    }

    private ResponseEntity<?> montarResposta(
            HttpStatus status, String mensagem, Object campos, HttpServletRequest requisicao) {
        Map<String, Object> corpo = Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "message", mensagem,
            "path", requisicao.getRequestURI(),
            "fields", campos);

        return ResponseEntity.status(status).body(corpo);
    }
}
