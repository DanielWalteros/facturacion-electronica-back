package co.com.segurosbolivar.facturacionelectronica.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void shouldSetXContentTypeOptionsHeader() throws IOException, ServletException {
        filter.doFilter(request, response, chain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
    }

    @Test
    void shouldSetStrictTransportSecurityHeader() throws IOException, ServletException {
        filter.doFilter(request, response, chain);

        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    }

    @Test
    void shouldContinueFilterChain() throws IOException, ServletException {
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldSetBothHeadersBeforeContinuingChain() throws IOException, ServletException {
        filter.doFilter(request, response, chain);

        var inOrder = inOrder(response, chain);
        inOrder.verify(response).setHeader("X-Content-Type-Options", "nosniff");
        inOrder.verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        inOrder.verify(chain).doFilter(request, response);
    }
}
