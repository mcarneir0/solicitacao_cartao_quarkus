package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.entity.SolicitacaoCartao;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.repository.CartaoRepository;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

/**
 * Cenários unitários do caso de uso com repositórios simulados.
 */
class SolicitacaoUseCaseTest {
    SolicitacaoCartaoRepository solicitacoes;
    UsuarioRepository usuarios;
    TipoCartaoRepository tiposCartao;
    CartaoRepository cartoes;

    SolicitacaoUseCase useCase;

    @BeforeEach
    void setUp() {
        solicitacoes = mock(SolicitacaoCartaoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        tiposCartao = mock(TipoCartaoRepository.class);
        cartoes = mock(CartaoRepository.class);

        useCase = new SolicitacaoUseCase();
        useCase.solicitacoes = solicitacoes;
        useCase.usuarios = usuarios;
        useCase.tiposCartao = tiposCartao;
        useCase.cartoes = cartoes;
    }

    @Test
    void buscar_quandoPedidoNaoForDoTitular_deveRetornar404() {
        when(solicitacoes.buscarPorIdETitular(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscar(1L, 99L));
    }

    @Test
    void cancelar_quandoPedidoNaoEstiverPendente_deveRetornar409() {
        SolicitacaoCartao solicitacao = novaSolicitacao(1L, 10L);
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        when(solicitacoes.buscarPorIdETitularComBloqueio(99L, 1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 99L));
    }

    @Test
    void emitir_quandoPedidoJaEmitido_deveRetornar409SemCriarSegundoCartao() {
        SolicitacaoCartao solicitacao = novaSolicitacao(1L, 10L);
        when(solicitacoes.buscarPorIdETitularComBloqueio(99L, 1L)).thenReturn(Optional.of(solicitacao));
        when(cartoes.existePorSolicitacao(solicitacao.getId())).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> useCase.emitir(1L, 99L));
        verify(cartoes, never()).salvar(any());
    }

    @Test
    void criar_quandoTipoIndisponivel_deveRetornar409() {
        Usuario usuario = new Usuario("Aluno", "aluno@example.com", "$2a$12$hash");
        usuario.setId(1L);
        TipoCartao tipo = new TipoCartao("ESSENCIAL", "desc", false);
        tipo.setId(10L);

        when(usuarios.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(usuario));
        when(tiposCartao.buscarPorId(10L)).thenReturn(Optional.of(tipo));

        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, new SolicitacaoRequest(10L)));
        verify(solicitacoes, never()).salvar(any());
    }

    @Test
    void criar_quandoJaExistirPendenteMesmoTipo_deveRetornar409() {
        Usuario usuario = new Usuario("Aluno", "aluno@example.com", "$2a$12$hash");
        usuario.setId(1L);
        TipoCartao tipo = new TipoCartao("ESSENCIAL", "desc", true);
        tipo.setId(10L);

        when(usuarios.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(usuario));
        when(tiposCartao.buscarPorId(10L)).thenReturn(Optional.of(tipo));
        when(solicitacoes.existePendente(1L, 10L)).thenReturn(true);

        SolicitacaoRequest request = new SolicitacaoRequest(10L);
        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, request));
        verify(solicitacoes, never()).salvar(any());
    }

    private SolicitacaoCartao novaSolicitacao(Long usuarioId, Long tipoId) {
        Usuario usuario = new Usuario("Aluno", "aluno@example.com", "$2a$12$hash");
        usuario.setId(usuarioId);

        TipoCartao tipo = new TipoCartao("ESSENCIAL", "desc", true);
        tipo.setId(tipoId);

        SolicitacaoCartao solicitacao = new SolicitacaoCartao(usuario, tipo);
        solicitacao.setId(99L);
        return solicitacao;
    }
}
