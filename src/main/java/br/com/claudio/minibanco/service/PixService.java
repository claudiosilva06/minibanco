package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.dto.ComprovantePixResponse;
import br.com.claudio.minibanco.exception.ChavePixJaCadastradaException;
import br.com.claudio.minibanco.exception.ChavePixNaoEncontradaException;
import br.com.claudio.minibanco.exception.ContaNaoEncontradaException;
import br.com.claudio.minibanco.exception.LimiteDeChavesPixException;
import br.com.claudio.minibanco.model.ChavePix;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoChavePix;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import br.com.claudio.minibanco.repository.ChavePixRepository;
import br.com.claudio.minibanco.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class PixService {

    static final int LIMITE_DE_CHAVES_POR_CONTA = 5;

    private final ChavePixRepository chavePixRepository;
    private final ContaRepository contaRepository;
    private final TransferenciaService transferenciaService;

    public PixService(ChavePixRepository chavePixRepository,
                      ContaRepository contaRepository,
                      TransferenciaService transferenciaService) {
        this.chavePixRepository = chavePixRepository;
        this.contaRepository = contaRepository;
        this.transferenciaService = transferenciaService;
    }

    @Transactional
    public ChavePix cadastrarChave(Long contaId, TipoChavePix tipo, String chave) {
        // Trava a conta para que cadastros simultâneos não passem do limite de chaves.
        Conta conta = contaRepository.findByIdParaAtualizacao(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId));

        String chaveNormalizada = tipo.normalizar(chave);
        if (chavePixRepository.existsByChave(chaveNormalizada)) {
            throw new ChavePixJaCadastradaException(chaveNormalizada);
        }
        if (chavePixRepository.countByContaId(contaId) >= LIMITE_DE_CHAVES_POR_CONTA) {
            throw new LimiteDeChavesPixException(LIMITE_DE_CHAVES_POR_CONTA);
        }
        return chavePixRepository.save(new ChavePix(conta, tipo, chaveNormalizada));
    }

    @Transactional(readOnly = true)
    public List<ChavePix> listarChaves(Long contaId) {
        if (!contaRepository.existsById(contaId)) {
            throw new ContaNaoEncontradaException(contaId);
        }
        return chavePixRepository.findByContaIdOrderById(contaId);
    }

    @Transactional
    public void removerChave(Long contaId, String chave) {
        ChavePix chavePix = localizar(chave);
        if (!chavePix.getConta().getId().equals(contaId)) {
            // Não revela que a chave existe em outra conta.
            throw new ChavePixNaoEncontradaException(chave);
        }
        chavePixRepository.delete(chavePix);
    }

    @Transactional(readOnly = true)
    public ChavePix consultar(String chave) {
        return localizar(chave);
    }

    @Transactional
    public ComprovantePixResponse pagar(Long origemId, String chave, BigDecimal valor) {
        ChavePix destino = localizar(chave);
        Conta recebedor = destino.getConta();
        if (recebedor.getId().equals(origemId)) {
            throw new IllegalArgumentException("Não é possível fazer Pix para a própria conta");
        }

        Movimentacao envio = transferenciaService.transferir(origemId, recebedor.getId(), valor,
                TipoMovimentacao.PIX_ENVIADO, TipoMovimentacao.PIX_RECEBIDO);

        return new ComprovantePixResponse(destino.getChave(), recebedor.getTitular(),
                envio.getValor(), envio.getDataHora());
    }

    /**
     * Encontra a chave do jeito que o usuário digitou: "123.456.789-09", "Maria@Email.com"
     * e "+55 (11) 98765-4321" acham as chaves guardadas sem formatação.
     */
    private ChavePix localizar(String chave) {
        String digitada = chave.trim();
        return Stream.concat(Stream.of(digitada), formasNormalizadas(digitada))
                .distinct()
                .map(chavePixRepository::findByChave)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new ChavePixNaoEncontradaException(digitada));
    }

    private Stream<String> formasNormalizadas(String chave) {
        return Stream.of(TipoChavePix.CPF, TipoChavePix.EMAIL, TipoChavePix.TELEFONE)
                .flatMap(tipo -> {
                    try {
                        return Stream.of(tipo.normalizar(chave));
                    } catch (IllegalArgumentException e) {
                        return Stream.empty();
                    }
                });
    }
}
