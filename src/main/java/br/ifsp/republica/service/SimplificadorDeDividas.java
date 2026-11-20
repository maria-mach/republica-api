package br.ifsp.republica.service;

import br.ifsp.republica.dto.Contratos.Transferencia;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Compensa saldos apenas em memória: devedores e credores são pareados em ordem de morador.
 * As sugestões não registram pagamentos no banco.
 */
final class SimplificadorDeDividas {

    private SimplificadorDeDividas() {
    }

    static List<Transferencia> simplificar(Map<Long, BigDecimal> saldosPorMorador) {
        List<Map.Entry<Long, BigDecimal>> devedores = copiarSaldosQue(saldosPorMorador, saldo -> saldo.signum() < 0);
        List<Map.Entry<Long, BigDecimal>> credores = copiarSaldosQue(saldosPorMorador, saldo -> saldo.signum() > 0);
        List<Transferencia> transferencias = new ArrayList<>();

        int indiceDevedor = 0;
        int indiceCredor = 0;
        while (indiceDevedor < devedores.size() && indiceCredor < credores.size()) {
            Map.Entry<Long, BigDecimal> devedor = devedores.get(indiceDevedor);
            Map.Entry<Long, BigDecimal> credor = credores.get(indiceCredor);
            BigDecimal valor = devedor.getValue().negate().min(credor.getValue());

            transferencias.add(new Transferencia(devedor.getKey(), credor.getKey(), valor));
            devedor.setValue(devedor.getValue().add(valor));
            credor.setValue(credor.getValue().subtract(valor));

            if (devedor.getValue().signum() == 0) {
                indiceDevedor++;
            }
            if (credor.getValue().signum() == 0) {
                indiceCredor++;
            }
        }

        return transferencias;
    }

    private static List<Map.Entry<Long, BigDecimal>> copiarSaldosQue(
            Map<Long, BigDecimal> saldosPorMorador, Predicate<BigDecimal> criterio) {
        return saldosPorMorador.entrySet().stream()
            .filter(saldo -> criterio.test(saldo.getValue()))
            .<Map.Entry<Long, BigDecimal>>map(saldo -> new SimpleEntry<>(saldo.getKey(), saldo.getValue()))
            .toList();
    }
}
