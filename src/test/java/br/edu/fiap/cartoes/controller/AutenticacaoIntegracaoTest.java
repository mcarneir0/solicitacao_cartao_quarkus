package br.edu.fiap.cartoes.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AutenticacaoIntegracaoTest {

    @Test
    void login_comEmailInexistente_deveRetornar401ComMensagemUnificada() {
        String payload = """
            {
              "email": "naoexiste@example.com",
              "senha": "SenhaQualquer123"
            }
            """;

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("mensagem", equalTo("E-mail ou senha inválidos"));
    }

    @Test
    void login_comSenhaIncorreta_deveRetornar401ComMensagemUnificada() {
        String payload = """
            {
              "email": "ana@example.com",
              "senha": "SenhaIncorreta123"
            }
            """;

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("mensagem", equalTo("E-mail ou senha inválidos"));
    }

    @Test
    void login_comCredenciaisValidas_deveRetornarTokenCom900Segundos() {
        String payload = """
            {
              "email": "ana@example.com",
              "senha": "Cartoes@123"
            }
            """;

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresIn", equalTo(900));
    }
}