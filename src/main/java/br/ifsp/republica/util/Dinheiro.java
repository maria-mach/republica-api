package br.ifsp.republica.util;

import java.math.BigDecimal;

public final class Dinheiro {

    public static final int CASAS_DECIMAIS = 2;

    private Dinheiro() {
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(CASAS_DECIMAIS);
    }
}
