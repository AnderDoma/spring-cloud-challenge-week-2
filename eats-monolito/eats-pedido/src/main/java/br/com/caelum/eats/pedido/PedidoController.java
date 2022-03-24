package br.com.caelum.eats.pedido;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
class PedidoController {

	private static Logger logger = LoggerFactory.getLogger(PedidoController.class);
	
	private PedidoRepository repo;

	@GetMapping("/pedidos")
	List<PedidoDto> lista() {
		return repo.findAll()
				.stream()
				.map(PedidoDto::new)
				.collect(Collectors.toList());
	}


	@GetMapping("/pedidos/{id}")
	PedidoDto porId(@PathVariable("id") Long id) {
		Pedido pedido = repo.findById(id).orElseThrow(ResourceNotFoundException::new);
		logger.info("Consulta de pedido realizada");
		return new PedidoDto(pedido);
	}

	@PostMapping("/pedidos")
	PedidoDto adiciona(@RequestBody Pedido pedido) {
		pedido.setDataHora(LocalDateTime.now());
		pedido.setStatus(Pedido.Status.REALIZADO);
		pedido.getItens().forEach(item -> item.setPedido(pedido));
		pedido.getEntrega().setPedido(pedido);
		Pedido salvo = repo.save(pedido);
		logger.info("O pedido foi registrado.");
		return new PedidoDto(salvo);
	}

	@HystrixCommand(fallbackMethod = "realizaFallbackPedido",
	threadPoolKey = "putAtualizaStatusThreadPool")
	@PutMapping("/pedidos/{pedidoId}/status")
	PedidoDto atualizaStatus(@PathVariable Long pedidoId, @RequestBody Pedido pedidoParaAtualizar) throws InterruptedException {
		if (LocalDateTime.now().getMinute() % 2 == 0) {
			Pedido pedido = repo.porIdComItens(pedidoId).orElseThrow(ResourceNotFoundException::new);
			pedido.setStatus(pedidoParaAtualizar.getStatus());
			repo.atualizaStatus(pedido.getStatus(), pedido);
			return new PedidoDto(pedido);
		}
		logger.info("FALLBACK EM ANDAMENTO");
		Thread.sleep(30000);
		throw new RuntimeException("Não foi possível atualizar o pedido");
	}

	@PutMapping("/pedidos/{id}/pago")
	void pago(@PathVariable("id") Long id) {
		Pedido pedido = repo.porIdComItens(id).orElseThrow(ResourceNotFoundException::new);
		pedido.setStatus(Pedido.Status.PAGO);
		repo.atualizaStatus(Pedido.Status.PAGO, pedido);
	}


	@GetMapping("/parceiros/restaurantes/{restauranteId}/pedidos/pendentes")
	List<PedidoDto> pendentes(@PathVariable("restauranteId") Long restauranteId) {
		return repo.doRestauranteSemOsStatus(restauranteId, Arrays.asList(Pedido.Status.REALIZADO, Pedido.Status.ENTREGUE)).stream()
				.map(pedido -> new PedidoDto(pedido)).collect(Collectors.toList());
	}
	
	public PedidoDto realizaFallbackPedido(Long pedidoId, Pedido pedidoParaAtualizar) {
		logger.info("FALLBACK REALIZADO");
		Pedido pedido = repo.porIdComItens(pedidoId).orElseThrow(ResourceNotFoundException::new);
		pedido.setStatus(Pedido.Status.PROCESSADO);
		repo.atualizaStatus(pedido.getStatus(), pedido);
		return new PedidoDto(pedido);
	}

}
