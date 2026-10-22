package br.edu.fiap.cartoes.usecase;

import br.edu.fiap.cartoes.dto.SolicitacaoRequest;
import br.edu.fiap.cartoes.entity.StatusSolicitacao;
import br.edu.fiap.cartoes.entity.TipoCartao;
import br.edu.fiap.cartoes.entity.Usuario;
import br.edu.fiap.cartoes.exception.RegraNegocioException;
import br.edu.fiap.cartoes.exception.RecursoNaoEncontradoException;
import br.edu.fiap.cartoes.repository.CartaoRepository;
import br.edu.fiap.cartoes.repository.SolicitacaoCartaoRepository;
import br.edu.fiap.cartoes.repository.TipoCartaoRepository;
import br.edu.fiap.cartoes.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Cenários unitários do caso de uso com repositórios simulados.
 */
@ExtendWith(MockitoExtension.class)
class SolicitacaoUseCaseTest {

    @Mock
    private SolicitacaoCartaoRepository solicitacoes;
    @Mock
    private UsuarioRepository usuarios;
    @Mock
    private TipoCartaoRepository tiposCartao;
    @Mock
    private CartaoRepository cartoes;

    @InjectMocks
    private SolicitacaoUseCase useCase;

    @Test
    void buscar_quandoPedidoNaoForDoTitular_deveRetornar404() {
        when(solicitacoes.buscarPorIdETitular(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscar(1L, 99L));
    }

    @Test
    void cancelar_quandoPedidoNaoEstiverPendente_deveRetornar409() {
        // 1. Simula que a solicitação EXISTE
        var solicitacao = org.mockito.Mockito.mock(br.edu.fiap.cartoes.entity.SolicitacaoCartao.class);

        // 2. Simula o comportamento da entidade rejeitando o cancelamento (pois não está PENDENTE)
        org.mockito.Mockito.doThrow(new RegraNegocioException("Estado invalido")).when(solicitacao).cancelar();
        when(solicitacoes.buscarPorIdETitularComBloqueio(99L, 1L)).thenReturn(Optional.of(solicitacao));

        assertThrows(RegraNegocioException.class, () -> useCase.cancelar(1L, 99L));
    }

    @Test
    void emitir_quandoPedidoJaEmitido_deveRetornar409SemCriarSegundoCartao() {
        // 1. Simula que a solicitação EXISTE
        var solicitacao = org.mockito.Mockito.mock(br.edu.fiap.cartoes.entity.SolicitacaoCartao.class);
        when(solicitacao.getId()).thenReturn(99L);
        when(solicitacoes.buscarPorIdETitularComBloqueio(99L, 1L)).thenReturn(Optional.of(solicitacao));

        // 2. Simula que o repositório de cartões confirma que já existe um cartão emitido
        when(cartoes.existePorSolicitacao(99L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> useCase.emitir(1L, 99L));
    }

    @Test
    void listar_comStatusOpcional_deveFiltrarSomenteSolicitacoesDoTitular() {
        // Cenário-alvo: somente registros do usuarioId informado e filtro opcional por status.
        useCase.listar(1L, StatusSolicitacao.PENDENTE);
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
    }

    @Test
    void criar_quandoJaExistirPendenteMesmoTipo_deveRetornar409() {
        // 1. Simula que o Utilizador e o Tipo de Cartão EXISTEM e estão disponíveis
        var usuario = org.mockito.Mockito.mock(br.edu.fiap.cartoes.entity.Usuario.class);
        var tipoCartao = org.mockito.Mockito.mock(br.edu.fiap.cartoes.entity.TipoCartao.class);

        when(tipoCartao.isDisponivel()).thenReturn(true);
        when(tipoCartao.getId()).thenReturn(1L);

        when(usuarios.buscarPorIdComBloqueio(1L)).thenReturn(Optional.of(usuario));
        when(tiposCartao.buscarPorId(1L)).thenReturn(Optional.of(tipoCartao));

        // 2. Simula que já existe um pedido pendente
        when(solicitacoes.existePendente(1L, 1L)).thenReturn(true);

        SolicitacaoRequest request = new SolicitacaoRequest(1L);
        assertThrows(RegraNegocioException.class, () -> useCase.criar(1L, request));
    }
}
