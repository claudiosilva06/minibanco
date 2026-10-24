package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.LimiteDiarioExcedidoException;
import br.com.claudio.minibanco.model.Conta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "minibanco.limite-diario-saida=500.00")
class LimiteDiarioTest {

    @Autowired
    private ContaService contaService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Test
    void devePermitirSaquesAteOLimite() {
        Conta conta = contaComSaldo("1000");

        contaService.sacar(conta.getId(), new BigDecimal("300"));
        contaService.sacar(conta.getId(), new BigDecimal("200"));

        assertThat(saldoDe(conta)).isEqualByComparingTo("500");
    }

    @Test
    void deveRecusarSaqueQuePassaDoLimite() {
        Conta conta = contaComSaldo("1000");
        contaService.sacar(conta.getId(), new BigDecimal("400"));

        assertThatThrownBy(() -> contaService.sacar(conta.getId(), new BigDecimal("150")))
                .isInstanceOf(LimiteDiarioExcedidoException.class)
                .hasMessage("Limite diário de saídas excedido. Disponível hoje: R$ 100.00");

        assertThat(saldoDe(conta)).isEqualByComparingTo("600");
    }

    @Test
    void transferenciasEnviadasContamNoLimite() {
        Conta origem = contaComSaldo("1000");
        Conta destino = contaService.criar("Ana");
        transferenciaService.transferir(origem.getId(), destino.getId(), new BigDecimal("450"));

        assertThatThrownBy(() -> contaService.sacar(origem.getId(), new BigDecimal("100")))
                .isInstanceOf(LimiteDiarioExcedidoException.class);
        assertThatThrownBy(() ->
                transferenciaService.transferir(origem.getId(), destino.getId(), new BigDecimal("100")))
                .isInstanceOf(LimiteDiarioExcedidoException.class);

        assertThat(saldoDe(origem)).isEqualByComparingTo("550");
        assertThat(saldoDe(destino)).isEqualByComparingTo("450");
    }

    @Test
    void depositosETransferenciasRecebidasNaoContamNoLimite() {
        Conta conta = contaComSaldo("2000");
        Conta outra = contaComSaldo("500");
        transferenciaService.transferir(outra.getId(), conta.getId(), new BigDecimal("500"));

        contaService.sacar(conta.getId(), new BigDecimal("500"));

        assertThat(saldoDe(conta)).isEqualByComparingTo("2000");
    }

    private Conta contaComSaldo(String saldo) {
        Conta conta = contaService.criar("Mano");
        contaService.depositar(conta.getId(), new BigDecimal(saldo));
        return conta;
    }

    private BigDecimal saldoDe(Conta conta) {
        return contaService.buscar(conta.getId()).getSaldo();
    }
}
