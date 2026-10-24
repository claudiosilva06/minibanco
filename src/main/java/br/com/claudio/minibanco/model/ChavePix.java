package br.com.claudio.minibanco.model;

import jakarta.persistence.*;

@Entity
public class ChavePix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 77)
    private String chave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoChavePix tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    protected ChavePix() {
    }

    public ChavePix(Conta conta, TipoChavePix tipo, String chave) {
        this.conta = conta;
        this.tipo = tipo;
        this.chave = chave;
    }

    public Long getId() {
        return id;
    }

    public String getChave() {
        return chave;
    }

    public TipoChavePix getTipo() {
        return tipo;
    }

    public Conta getConta() {
        return conta;
    }
}
