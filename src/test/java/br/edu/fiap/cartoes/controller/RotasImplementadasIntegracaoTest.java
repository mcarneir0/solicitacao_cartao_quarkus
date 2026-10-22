package br.edu.fiap.cartoes.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class RotasImplementadasIntegracaoTest {

    @Test
    void nenhumaRotaDeveRetornar501Todo() {
        String token = gerarTokenValido();
        String auth = "Bearer " + token;

        // Rotas Públicas
        given().contentType("application/json").body("{}")
                .when().post("/api/usuarios").then().statusCode(not(501));

        given().contentType("application/json").body("{}")
                .when().post("/api/auth/login").then().statusCode(not(501));

        // Rotas Protegidas (GET)
        given().header("Authorization", auth)
                .when().get("/api/tipos-cartao").then().statusCode(not(501));

        given().header("Authorization", auth)
                .when().get("/api/solicitacoes").then().statusCode(not(501));

        given().header("Authorization", auth)
                .when().get("/api/solicitacoes/1").then().statusCode(not(501));

        given().header("Authorization", auth)
                .when().get("/api/cartoes").then().statusCode(not(501));

        // Rotas Protegidas (POST/PATCH de Negócio)
        given().header("Authorization", auth).contentType("application/json").body("{\"tipoCartaoId\": 1}")
                .when().post("/api/solicitacoes").then().statusCode(not(501));

        given().header("Authorization", auth)
                .when().patch("/api/solicitacoes/1/cancelar").then().statusCode(not(501));

        given().header("Authorization", auth)
                .when().post("/api/solicitacoes/1/emitir").then().statusCode(not(501));
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