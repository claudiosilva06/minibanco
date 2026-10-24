package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.ContaNaoEncontradaException;
import br.com.claudio.minibanco.exception.SaldoInsuficienteException;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ContaServiceTest {

    private static final PageRequest MAIS_RECENTES =
            PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dataHora", "id"));

    @Autowired
    private ContaService contaService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Test
    void extratoDeveListarMovimentacoesDaMaisRecenteParaAMaisAntiga() {
        Conta conta = contaService.criar("Mano");
        contaService.depositar(conta.getId(), new BigDecimal("100"));
        contaService.sacar(conta.getId(), new BigDecimal("40"));

        Page<Movimentacao> extrato = contaService.extrato(conta.getId(), MAIS_RECENTES);

        assertThat(extrato.getContent())
                .extracting(Movimentacao::getTipo)
                .containsExactly(TipoMovimentacao.SAQUE, TipoMovimentacao.DEPOSITO);
        assertThat(extrato.getContent().get(0).getValor()).isEqualByComparingTo("40");
    }

    @Test
    void extratoDeveRegistrarOsDoisLadosDaTransferencia() {
        Conta origem = contaService.criar("Mano");
        Conta destino = contaService.criar("Ana");
        contaService.depositar(origem.getId(), new BigDecimal("100"));

        transferenciaService.transferir(origem.getId(), destino.getId(), new BigDecimal("25"));

        assertThat(contaService.extrato(origem.getId(), MAIS_RECENTES).getContent())
                .extracting(Movimentacao::getTipo)
                .containsExactly(TipoMovimentacao.TRANSFERENCIA_ENVIADA, TipoMovimentacao.DEPOSITO);
        assertThat(contaService.extrato(destino.getId(), MAIS_RECENTES).getContent())
                .extracting(Movimentacao::getTipo)
                .containsExactly(TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
    }

    @Test
    void extratoDeveSerPaginado() {
        Conta conta = contaService.criar("Mano");
        for (int i = 0; i < 5; i++) {
            contaService.depositar(conta.getId(), BigDecimal.TEN);
        }

        Page<Movimentacao> pagina = contaService.extrato(conta.getId(), PageRequest.of(0, 2));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalElements()).isEqualTo(5);
        assertThat(pagina.getTotalPages()).isEqualTo(3);
    }

    @Test
    void saqueRecusadoNaoDeveAparecerNoExtrato() {
        Conta conta = contaService.criar("Mano");

        assertThatThrownBy(() -> contaService.sacar(conta.getId(), BigDecimal.ONE))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(contaService.extrato(conta.getId(), MAIS_RECENTES).getContent()).isEmpty();
    }

    @Test
    void extratoDeContaInexistenteDeveFalhar() {
        assertThatThrownBy(() -> contaService.extrato(9999L, MAIS_RECENTES))
                .isInstanceOf(ContaNaoEncontradaException.class);
    }
}
