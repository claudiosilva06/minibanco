package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.dto.ComprovantePixResponse;
import br.com.claudio.minibanco.exception.ChavePixJaCadastradaException;
import br.com.claudio.minibanco.exception.ChavePixNaoEncontradaException;
import br.com.claudio.minibanco.exception.LimiteDeChavesPixException;
import br.com.claudio.minibanco.exception.SaldoInsuficienteException;
import br.com.claudio.minibanco.model.ChavePix;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoChavePix;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PixServiceTest {

    @Autowired
    private PixService pixService;

    @Autowired
    private ContaService contaService;

    @Test
    void devePagarPixPelaChaveDoRecebedor() {
        Conta pagador = contaComSaldo("Mano", "100");
        Conta recebedor = contaService.criar("Ana Lima");
        String email = emailUnico();
        pixService.cadastrarChave(recebedor.getId(), TipoChavePix.EMAIL, email);

        ComprovantePixResponse comprovante = pixService.pagar(pagador.getId(), email, new BigDecimal("35.50"));

        assertThat(comprovante.recebedor()).isEqualTo("Ana Lima");
        assertThat(comprovante.valor()).isEqualByComparingTo("35.50");
        assertThat(comprovante.dataHora()).isNotNull();
        assertThat(saldoDe(pagador)).isEqualByComparingTo("64.50");
        assertThat(saldoDe(recebedor)).isEqualByComparingTo("35.50");
        assertThat(tiposNoExtrato(pagador)).containsExactly(TipoMovimentacao.PIX_ENVIADO, TipoMovimentacao.DEPOSITO);
        assertThat(tiposNoExtrato(recebedor)).containsExactly(TipoMovimentacao.PIX_RECEBIDO);
    }

    @Test
    void deveEncontrarAChaveDigitadaComFormatacao() {
        Conta pagador = contaComSaldo("Mano", "100");
        Conta recebedor = contaService.criar("Ana");
        pixService.cadastrarChave(recebedor.getId(), TipoChavePix.CPF, "11144477735");

        pixService.pagar(pagador.getId(), "111.444.777-35", BigDecimal.TEN);

        assertThat(saldoDe(recebedor)).isEqualByComparingTo("10");
    }

    @Test
    void naoDeveCadastrarChaveRepetida() {
        Conta conta = contaService.criar("Mano");
        Conta outra = contaService.criar("Ana");
        pixService.cadastrarChave(conta.getId(), TipoChavePix.TELEFONE, "+5511912345678");

        assertThatThrownBy(() ->
                pixService.cadastrarChave(outra.getId(), TipoChavePix.TELEFONE, "+55 (11) 91234-5678"))
                .isInstanceOf(ChavePixJaCadastradaException.class);
    }

    @Test
    void contaPodeTerNoMaximoCincoChaves() {
        Conta conta = contaService.criar("Mano");
        for (int i = 0; i < PixService.LIMITE_DE_CHAVES_POR_CONTA; i++) {
            pixService.cadastrarChave(conta.getId(), TipoChavePix.ALEATORIA, null);
        }

        assertThatThrownBy(() -> pixService.cadastrarChave(conta.getId(), TipoChavePix.ALEATORIA, null))
                .isInstanceOf(LimiteDeChavesPixException.class);
        assertThat(pixService.listarChaves(conta.getId())).hasSize(5);
    }

    @Test
    void naoDeveFazerPixParaAPropriaConta() {
        Conta conta = contaComSaldo("Mano", "100");
        ChavePix chave = pixService.cadastrarChave(conta.getId(), TipoChavePix.ALEATORIA, null);

        assertThatThrownBy(() -> pixService.pagar(conta.getId(), chave.getChave(), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é possível fazer Pix para a própria conta");
    }

    @Test
    void pixSemSaldoNaoDeveMoverDinheiro() {
        Conta pagador = contaComSaldo("Mano", "5");
        Conta recebedor = contaService.criar("Ana");
        ChavePix chave = pixService.cadastrarChave(recebedor.getId(), TipoChavePix.ALEATORIA, null);

        assertThatThrownBy(() -> pixService.pagar(pagador.getId(), chave.getChave(), BigDecimal.TEN))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(saldoDe(pagador)).isEqualByComparingTo("5");
        assertThat(saldoDe(recebedor)).isEqualByComparingTo("0");
    }

    @Test
    void pixParaChaveInexistenteDeveFalhar() {
        Conta pagador = contaComSaldo("Mano", "100");

        assertThatThrownBy(() -> pixService.pagar(pagador.getId(), emailUnico(), BigDecimal.ONE))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void chaveRemovidaNaoRecebeMaisPix() {
        Conta recebedor = contaService.criar("Ana");
        String email = emailUnico();
        pixService.cadastrarChave(recebedor.getId(), TipoChavePix.EMAIL, email);

        pixService.removerChave(recebedor.getId(), email);

        assertThatThrownBy(() -> pixService.consultar(email))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
    }

    @Test
    void naoDeveRemoverChaveDeOutraConta() {
        Conta dona = contaService.criar("Ana");
        Conta outra = contaService.criar("Mano");
        String email = emailUnico();
        pixService.cadastrarChave(dona.getId(), TipoChavePix.EMAIL, email);

        assertThatThrownBy(() -> pixService.removerChave(outra.getId(), email))
                .isInstanceOf(ChavePixNaoEncontradaException.class);
        assertThat(pixService.consultar(email).getConta().getId()).isEqualTo(dona.getId());
    }

    private String emailUnico() {
        return UUID.randomUUID() + "@email.com";
    }

    private Conta contaComSaldo(String titular, String saldo) {
        Conta conta = contaService.criar(titular);
        contaService.depositar(conta.getId(), new BigDecimal(saldo));
        return conta;
    }

    private BigDecimal saldoDe(Conta conta) {
        return contaService.buscar(conta.getId()).getSaldo();
    }

    private Iterable<TipoMovimentacao> tiposNoExtrato(Conta conta) {
        return contaService.extrato(conta.getId(), PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dataHora", "id")))
                .map(Movimentacao::getTipo);
    }
}
