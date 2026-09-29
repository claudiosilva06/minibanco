package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.ContaNaoEncontradaException;
import br.com.claudio.minibanco.exception.SaldoInsuficienteException;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.repository.ContaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransferenciaServiceTest {

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private ContaRepository contaRepository;

    @Test
    void deveTransferirEntreContas() {
        Conta origem = criarConta("Mano", "100");
        Conta destino = criarConta("Ana", "50");

        transferenciaService.transferir(origem.getId(), destino.getId(), new BigDecimal("30"));

        assertThat(saldoDe(origem)).isEqualByComparingTo("70");
        assertThat(saldoDe(destino)).isEqualByComparingTo("80");
    }

    @Test
    void naoDeveAlterarNenhumaContaQuandoSaldoForInsuficiente() {
        Conta origem = criarConta("Mano", "100");
        Conta destino = criarConta("Ana", "50");

        assertThatThrownBy(() ->
                transferenciaService.transferir(origem.getId(), destino.getId(), new BigDecimal("500")))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(saldoDe(origem)).isEqualByComparingTo("100");
        assertThat(saldoDe(destino)).isEqualByComparingTo("50");
    }

    @Test
    void deveFalharQuandoContaDestinoNaoExiste() {
        Conta origem = criarConta("Mano", "100");

        assertThatThrownBy(() ->
                transferenciaService.transferir(origem.getId(), 9999L, new BigDecimal("10")))
                .isInstanceOf(ContaNaoEncontradaException.class);

        assertThat(saldoDe(origem)).isEqualByComparingTo("100");
    }

    private Conta criarConta(String titular, String saldoInicial) {
        Conta conta = new Conta(titular);
        conta.depositar(new BigDecimal(saldoInicial));
        return contaRepository.save(conta);
    }

    private BigDecimal saldoDe(Conta conta) {
        return contaRepository.findById(conta.getId()).orElseThrow().getSaldo();
    }
}