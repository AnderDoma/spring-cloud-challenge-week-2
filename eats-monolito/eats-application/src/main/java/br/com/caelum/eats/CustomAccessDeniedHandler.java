package br.com.caelum.eats;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc)
			throws IOException, ServletException {

		response.sendError(HttpStatus.FORBIDDEN.value(), "Você não possui o acesso para essa operação.");
	}
}
