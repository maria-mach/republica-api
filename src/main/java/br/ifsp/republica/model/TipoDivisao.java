package br.ifsp.republica.model;

public enum TipoDivisao {
    IGUAL,
    PERCENTUAL,
    VALOR;

    /** Expressão usada na validação do contrato da API, que continua recebendo texto. */
    public static final String PADRAO_DO_CONTRATO = "IGUAL|PERCENTUAL|VALOR";
}
