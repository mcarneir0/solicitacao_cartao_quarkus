package br.edu.fiap.cartoes.entity;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Cenários de transição de estado da entidade.
 * Ativar quando Dev 3 concluir os comportamentos cancelar/marcarEmitida.
 */
class SolicitacaoCartaoTest {

    @Test
    void cancelar_quandoPendente_deveIrParaCancelada() {
        SolicitacaoCartao solicitacao = novaSolicitacaoPendente();

        solicitacao.cancelar();

        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    @Test
    void cancelar_quandoNaoPendente_deveLancarRegraNegocioException() {
        SolicitacaoCartao solicitacao = novaSolicitacaoPendente();
        solicitacao.setStatus(StatusSolicitacao.EMITIDA);

        assertThrows(RegraNegocioException.class, solicitacao::cancelar);
    }

    @Test
    void marcarEmitida_quandoPendente_deveIrParaEmitida() {
        SolicitacaoCartao solicitacao = novaSolicitacaoPendente();

        solicitacao.marcarEmitida();

        assertEquals(StatusSolicitacao.EMITIDA, solicitacao.getStatus());
    }

    @Test
    void marcarEmitida_quandoNaoPendente_deveLancarRegraNegocioException() {
        SolicitacaoCartao solicitacao = novaSolicitacaoPendente();
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);

        assertThrows(RegraNegocioException.class, solicitacao::marcarEmitida);
    }

    private SolicitacaoCartao novaSolicitacaoPendente() {
        Usuario usuario = new Usuario("Aluno", "aluno@example.com", "$2a$12$hashsimulado");
        TipoCartao tipo = new TipoCartao("ESSENCIAL", "Cartao para o dia a dia", true);
        return new SolicitacaoCartao(usuario, tipo);
    }
}
