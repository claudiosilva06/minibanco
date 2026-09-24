package br.com.claudio.minibanco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String titular;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;


    protected Conta() {
    }

    public Conta(String titular) {
        this.titular = titular;
    }

    public Long getId() {
        return id;
    }

    public String getTitular() {
        return titular;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

}