package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.SaldoInsuficienteException;
import br.com.claudio.minibanco.model.Conta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcorrenciaTest {

    private static final int THREADS = 10;

    @Autowired
    private ContaService contaService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Test
    void saquesSimultaneosNaoDevemDeixarOSaldoNegativo() throws Exception {
        Conta conta = contaService.criar("Mano");
        contaService.depositar(conta.getId(), new BigDecimal("50"));

        List<Boolean> resultados = executarAoMesmoTempo(() -> {
            try {
                contaService.sacar(conta.getId(), BigDecimal.TEN);
                return true;
            } catch (SaldoInsuficienteException e) {
                return false;
            }
        });

        assertThat(resultados).filteredOn(ok -> ok).hasSize(5);
        assertThat(contaService.buscar(conta.getId()).getSaldo()).isEqualByComparingTo("0");
    }

    @Test
    void transferenciasCruzadasSimultaneasNaoDevemTravarNemPerderDinheiro() throws Exception {
        Conta a = contaService.criar("Ana");
        Conta b = contaService.criar("Bia");
        contaService.depositar(a.getId(), new BigDecimal("100"));
        contaService.depositar(b.getId(), new BigDecimal("100"));

        List<Boolean> resultados = executarAoMesmoTempo(alternando(
                () -> transferenciaService.transferir(a.getId(), b.getId(), BigDecimal.ONE),
                () -> transferenciaService.transferir(b.getId(), a.getId(), BigDecimal.ONE)));

        assertThat(resultados).containsOnly(true);
        assertThat(contaService.buscar(a.getId()).getSaldo()).isEqualByComparingTo("100");
        assertThat(contaService.buscar(b.getId()).getSaldo()).isEqualByComparingTo("100");
    }

    private Callable<Boolean> alternando(Runnable par, Runnable impar) {
        int[] contador = {0};
        return () -> {
            boolean vezDoPar;
            synchronized (contador) {
                vezDoPar = contador[0]++ % 2 == 0;
            }
            (vezDoPar ? par : impar).run();
            return true;
        };
    }

    private List<Boolean> executarAoMesmoTempo(Callable<Boolean> tarefa) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futuros = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                futuros.add(executor.submit(() -> {
                    largada.await();
                    return tarefa.call();
                }));
            }
            largada.countDown();

            List<Boolean> resultados = new ArrayList<>();
            for (Future<Boolean> futuro : futuros) {
                resultados.add(futuro.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }
}
