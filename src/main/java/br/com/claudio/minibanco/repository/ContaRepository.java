package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaRepository extends JpaRepository<Conta, Long> {
}