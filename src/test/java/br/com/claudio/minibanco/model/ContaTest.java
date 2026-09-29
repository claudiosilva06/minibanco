package br.com.claudio.minibanco.model;

import br.com.claudio.minibanco.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContaTest {

    @Test
    void contaNovaDeveComecarComSaldoZero() {
        Conta conta = new Conta("Mano");

        assertThat(conta.getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void depositoDeveAumentarOSaldo() {
        Conta conta = new Conta("Mano");

        conta.depositar(new BigDecimal("100.50"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("100.50");
    }

    @Test
    void saqueDeveDiminuirOSaldo() {
        Conta conta = new Conta("Mano");
        conta.depositar(new BigDecimal("100"));

        conta.sacar(new BigDecimal("30"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("70");
    }

    @Test
    void naoDevePermitirSacarMaisQueOSaldo() {
        Conta conta = new Conta("Mano");
        conta.depositar(new BigDecimal("50"));

        assertThatThrownBy(() -> conta.sacar(new BigDecimal("100")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("50");
    }

    @Test
    void naoDevePermitirDepositoNegativo() {
        Conta conta = new Conta("Mano");

        assertThatThrownBy(() -> conta.depositar(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}