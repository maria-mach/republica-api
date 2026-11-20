package br.ifsp.republica.util;

import br.ifsp.republica.exception.RegraException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public final class Competencias {

    private Competencias() {
    }

    public static YearMonth converter(String competencia) {
        try {
            return YearMonth.parse(competencia);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw RegraException.invalido("Mês deve ter formato AAAA-MM");
        }
    }
}
