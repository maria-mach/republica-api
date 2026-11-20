package br.ifsp.republica.security;

public final class ParametrosToken {

    public static final String EMISSOR = "republica-api";
    public static final String TIPO = "Bearer";
    public static final long VALIDADE_SEGUNDOS = 7200;
    public static final int LIMITE_SENHA_BYTES = 72;

    private ParametrosToken() {
    }
}
