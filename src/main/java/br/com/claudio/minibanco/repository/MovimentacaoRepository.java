package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByContaIdOrderByDataHoraDesc(Long contaId);
}