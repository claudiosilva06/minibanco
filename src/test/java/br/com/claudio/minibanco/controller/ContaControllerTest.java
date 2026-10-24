package br.com.claudio.minibanco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCriarContaComSaldoZero() throws Exception {
        postJson("/contas", "{\"titular\": \"Maria Souza\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.titular").value("Maria Souza"))
                .andExpect(jsonPath("$.saldo").value(0));
    }

    @Test
    void deveRecusarTitularVazio() throws Exception {
        postJson("/contas", "{\"titular\": \"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O nome do titular é obrigatório"));
    }

    @Test
    void deveRecusarJsonMalformado() throws Exception {
        postJson("/contas", "{titular")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido"));
    }

    @Test
    void deveRetornar404ParaContaInexistente() throws Exception {
        mockMvc.perform(get("/contas/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Conta 9999 não encontrada"));
    }

    @Test
    void deveDepositarESacar() throws Exception {
        long id = criarConta("Ana");

        postJson("/contas/" + id + "/deposito", "{\"valor\": 150.00}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.00));
        postJson("/contas/" + id + "/saque", "{\"valor\": 50.00}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(100.00));
    }

    @Test
    void deveRecusarValorNegativo() throws Exception {
        long id = criarConta("Ana");

        postJson("/contas/" + id + "/deposito", "{\"valor\": -5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O valor deve ser maior que zero"));
    }

    @Test
    void deveRetornar422QuandoSaldoForInsuficiente() throws Exception {
        long id = criarConta("Ana");

        postJson("/contas/" + id + "/saque", "{\"valor\": 10}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.erro").value("Saldo insuficiente"));
    }

    @Test
    void deveRetornarExtratoPaginado() throws Exception {
        long id = criarConta("Ana");
        postJson("/contas/" + id + "/deposito", "{\"valor\": 100}");
        postJson("/contas/" + id + "/saque", "{\"valor\": 30}");
        postJson("/contas/" + id + "/deposito", "{\"valor\": 5}");

        mockMvc.perform(get("/contas/" + id + "/extrato").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.content[0].valor").value(5))
                .andExpect(jsonPath("$.content[1].tipo").value("SAQUE"))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void deveTransferirEntreContas() throws Exception {
        long origem = criarConta("Ana");
        long destino = criarConta("Bia");
        postJson("/contas/" + origem + "/deposito", "{\"valor\": 100}");

        postJson("/transferencias",
                "{\"origemId\": " + origem + ", \"destinoId\": " + destino + ", \"valor\": 40}")
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/contas/" + destino))
                .andExpect(jsonPath("$.saldo").value(40));
    }

    @Test
    void naoDeveTransferirParaAMesmaConta() throws Exception {
        long id = criarConta("Ana");

        postJson("/transferencias", "{\"origemId\": " + id + ", \"destinoId\": " + id + ", \"valor\": 1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Não é possível transferir para a mesma conta"));
    }

    private long criarConta(String titular) throws Exception {
        String resposta = postJson("/contas", "{\"titular\": \"" + titular + "\"}")
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private ResultActions postJson(String url, String corpo) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
