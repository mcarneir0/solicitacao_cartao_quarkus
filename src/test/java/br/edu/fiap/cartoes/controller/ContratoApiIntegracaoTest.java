package br.edu.fiap.cartoes.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.Test;
import org.eclipse.microprofile.config.ConfigProvider;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testes de contrato independentes da implementacao dos use cases.
 * Valida exposicao de rotas no OpenAPI e formato padrao de erro para entradas invalidas.
 */
@QuarkusTest
class ContratoApiIntegracaoTest {

    @Test
    void tiposCartao_comTokenValido_deveRetornarDisponiveis() {
        String token = gerarTokenValido();

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/tipos-cartao")
        .then()
            .statusCode(200)
            .body("nome", hasItems("ESSENCIAL", "VIAGEM"));
    }

    @Test
    void openapi_deveConterAsNoveRotasDoDesafio() {
        given()
            .accept("application/json")
        .when()
            .get("/openapi")
        .then()
            .statusCode(200)
            .body(containsString("\"/api/usuarios\""))
            .body(containsString("\"/api/auth/login\""))
            .body(containsString("\"/api/tipos-cartao\""))
            .body(containsString("\"/api/solicitacoes\""))
            .body(containsString("\"/api/solicitacoes/{id}\""))
            .body(containsString("\"/api/solicitacoes/{id}/cancelar\""))
            .body(containsString("\"/api/solicitacoes/{id}/emitir\""))
            .body(containsString("\"/api/cartoes\""));
    }

    @Test
    void cadastro_payloadInvalido_deveRetornar400_comFormatoConstraintViolation() {
        String payloadInvalido = """
            {
              "nome": "",
              "email": "email-invalido",
              "senha": "123"
            }
            """;

        given()
            .contentType("application/json")
            .body(payloadInvalido)
        .when()
            .post("/api/usuarios")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("title", equalTo("Constraint Violation"))
            .body("violations", notNullValue())
            .body("violations.size()", equalTo(3));
    }

    @Test
    void login_payloadInvalido_deveRetornar400_comFormatoConstraintViolation() {
        String payloadInvalido = """
            {
              "email": "nao-e-email",
              "senha": ""
            }
            """;

        given()
            .contentType("application/json")
            .body(payloadInvalido)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("title", equalTo("Constraint Violation"))
            .body("violations", notNullValue())
            .body("violations.size()", equalTo(2));
    }

    private String gerarTokenValido() {
        String segredo = ConfigProvider.getConfig().getValue("security.jwt.secret", String.class);
        String emissor = ConfigProvider.getConfig().getValue("security.jwt.issuer", String.class);
        long agora = Instant.now().getEpochSecond();

        return Jwt.issuer(emissor)
                .subject("ana@example.com")
                .upn("ana@example.com")
                .issuedAt(agora)
                .expiresAt(agora + 900)
                .claim("usuarioId", 1L)
                .signWithSecret(segredo);
    }
}

