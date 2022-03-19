package br.com.caelum.eats.pagamento;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.caelum.eats.pagamento.Pagamento.Status;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/pagamentos")
@AllArgsConstructor
class PagamentoController {

	private PagamentoRepository pagamentoRepo;
	private PedidoClienteComFeign pedidoCliente;

	@GetMapping
	ResponseEntity<List<PagamentoDto>> lista() {
		return ResponseEntity.ok(pagamentoRepo.findAll()
				.stream()
				.map(PagamentoDto::new)
				.collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	@HystrixCommand(threadPoolKey = "getByIdPgtoThreadPool")
	PagamentoDto detalha(@PathVariable("id") Long id) {
		return pagamentoRepo.findById(id)
				.map(PagamentoDto::new)
				.orElseThrow(ResourceNotFoundException::new);
	}

	@PostMapping
	@HystrixCommand(threadPoolKey = "criaPagamentoThreadPool")
	ResponseEntity<PagamentoDto> cria(@RequestBody Pagamento pagamento, UriComponentsBuilder uriBuilder) {
		pagamento.setStatus(Pagamento.Status.CRIADO);
		Pagamento salvo = pagamentoRepo.save(pagamento);
		URI path = uriBuilder.path("/pagamentos/{id}").buildAndExpand(salvo.getId()).toUri();
		return ResponseEntity.created(path).body(new PagamentoDto(salvo));
	}

	@PutMapping("/{id}")
	@HystrixCommand(fallbackMethod = "realizaPagamentoFallback",
	threadPoolKey = "putPagamentoThreadPool")
	PagamentoDto confirma(@PathVariable("id") Long id) {
		Pagamento pagamento = pagamentoRepo.findById(id).orElseThrow(ResourceNotFoundException::new);
		pagamento.setStatus(Pagamento.Status.CONFIRMADO);
		pedidoCliente.notificaServicoDePedidoParaMudarStatus(pagamento.getPedidoId(), new MudancaDeStatusDoPedido("pago"));
		pagamento.setStatus(Status.CONFIRMADO);
		pagamentoRepo.save(pagamento);
		return new PagamentoDto(pagamento);
	}

	@DeleteMapping("/{id}")
	PagamentoDto cancela(@PathVariable("id") Long id) {
		Pagamento pagamento = pagamentoRepo.findById(id).orElseThrow(ResourceNotFoundException::new);
		pagamento.setStatus(Pagamento.Status.CANCELADO);
		pagamentoRepo.save(pagamento);
		return new PagamentoDto(pagamento);
	}
	
	public PagamentoDto realizaPagamentoFallback(Long id) {
		Pagamento pagamento = pagamentoRepo.findById(id).orElseThrow(ResourceNotFoundException::new);
		pagamento.setStatus(Status.PROCESSADO);
		pagamentoRepo.save(pagamento);
		return new PagamentoDto(pagamento);
	}

}