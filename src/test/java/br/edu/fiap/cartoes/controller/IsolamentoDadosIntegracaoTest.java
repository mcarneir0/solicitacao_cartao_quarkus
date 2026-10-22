package br.edu.fiap.cartoes.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class IsolamentoDadosIntegracaoTest {

    // Testes de operações bloqueadas (404) para registos de outro utilizador

    @Test
    void buscar_pedidoDeOutroTitular_deveRetornar404() {
        String tokenBruno = gerarTokenValido(2L, "bruno@example.com");

        given().header("Authorization", "Bearer " + tokenBruno)
                .when().get("/api/solicitacoes/1") // Pedido 1 é da Ana
                .then().statusCode(404);
    }

    @Test
    void cancelar_pedidoDeOutroTitular_deveRetornar404() {
        String tokenBruno = gerarTokenValido(2L, "bruno@example.com");

        given()
                .header("Authorization", "Bearer " + tokenBruno)
                .contentType("application/json")
                .when()
                .patch("/api/solicitacoes/1/cancelar")
                .then()
                .statusCode(404);
    }

    @Test
    void emitir_pedidoDeOutroTitular_deveRetornar404() {
        String tokenBruno = gerarTokenValido(2L, "bruno@example.com");

        given()
                .header("Authorization", "Bearer " + tokenBruno)
                .contentType("application/json")
                .when()
                .post("/api/solicitacoes/1/emitir")
                .then()
                .statusCode(404);
    }

    // Testes de isolamento em listagens

    @Test
    void listarSolicitacoes_naoDeveRetornarPedidosDeOutroTitular() {
        String tokenBruno = gerarTokenValido(2L, "bruno@example.com");

        given().header("Authorization", "Bearer " + tokenBruno)
                .when().get("/api/solicitacoes")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("id", hasItem(3)) // Apenas o pedido 3 pertence ao Bruno
                .body("id", not(hasItem(1))); // O pedido 1 da Ana não deve aparecer
    }

    @Test
    void listarCartoes_naoDeveRetornarCartoesDeOutroTitular() {
        String tokenBruno = gerarTokenValido(2L, "bruno@example.com");

        // O Bruno não tem nenhum cartão emitido na carga inicial
        given().header("Authorization", "Bearer " + tokenBruno)
                .when().get("/api/cartoes")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    private String gerarTokenValido(Long usuarioId, String email) {
        String segredo = ConfigProvider.getConfig().getValue("security.jwt.secret", String.class);
        String emissor = ConfigProvider.getConfig().getValue("security.jwt.issuer", String.class);
        long agora = Instant.now().getEpochSecond();

        return Jwt.issuer(emissor)
                .subject(email)
                .upn(email)
                .issuedAt(agora)
                .expiresAt(agora + 900)
                .claim("usuarioId", usuarioId)
                .signWithSecret(segredo);
    }
}