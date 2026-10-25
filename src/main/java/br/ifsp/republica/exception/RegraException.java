package br.ifsp.republica.exception;
import org.springframework.http.HttpStatus;

public class RegraException extends RuntimeException {
    public final HttpStatus status;
    public RegraException(HttpStatus status, String message) { super(message); this.status=status; }
    public static RegraException invalido(String msg) { return new RegraException(HttpStatus.BAD_REQUEST,msg); }
    public static RegraException proibido() { return new RegraException(HttpStatus.FORBIDDEN,"Operação não permitida para este morador"); }
    public static RegraException ausente() { return new RegraException(HttpStatus.NOT_FOUND,"Registro não encontrado na sua república"); }
}
