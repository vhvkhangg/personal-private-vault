package com.vhvkhangg.personalprivatevault.rag.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RagRetrievalIngressFilter implements Filter {

    public static final int MAX_INGRESS_BYTES = 65_536;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String path = httpRequest.getRequestURI();
            if ((path.startsWith("/api/v1/rag/retrievals") || path.startsWith("/api/v1/rag/qa")) && "POST".equalsIgnoreCase(httpRequest.getMethod())) {
                if (httpRequest.getContentLengthLong() > MAX_INGRESS_BYTES) {
                    sendLimitError(httpResponse);
                    return;
                }

                BoundedRequestWrapper wrapped = new BoundedRequestWrapper(httpRequest);
                try {
                    chain.doFilter(wrapped, response);
                } catch (Exception e) {
                    if (isScopeLimit(e)) {
                        sendLimitError(httpResponse);
                    } else {
                        throw e;
                    }
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isScopeLimit(Throwable t) {
        while (t != null) {
            if (t instanceof com.vhvkhangg.personalprivatevault.rag.api.RagQueryScopeLimitException) return true;
            t = t.getCause();
        }
        return false;
    }

    private void sendLimitError(HttpServletResponse response) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.setHeader("Cache-Control", "no-store");
            var body = ApiResponses.error("RAG_QUERY_SCOPE_LIMIT", "Request body exceeds 64 KiB limit");
            response.getOutputStream().write(objectMapper.writeValueAsBytes(body));
            response.getOutputStream().flush();
        }
    }

    private static class BoundedRequestWrapper extends HttpServletRequestWrapper {
        private ServletInputStream stream;
        private java.io.BufferedReader reader;

        public BoundedRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (stream == null) {
                stream = new BoundedServletInputStream(super.getInputStream());
            }
            return stream;
        }

        @Override
        public java.io.BufferedReader getReader() throws IOException {
            if (reader == null) reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            return reader;
        }
    }

    private static class BoundedServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private int bytesRead = 0;

        public BoundedServletInputStream(ServletInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b != -1) {
                bytesRead++;
                if (bytesRead > MAX_INGRESS_BYTES) {
                    throw new com.vhvkhangg.personalprivatevault.rag.api.RagQueryScopeLimitException("Request body exceeds 64 KiB limit");
                }
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = delegate.read(b, off, Math.min(len, MAX_INGRESS_BYTES - bytesRead + 1));
            if (n > 0) {
                bytesRead += n;
                if (bytesRead > MAX_INGRESS_BYTES) {
                    throw new com.vhvkhangg.personalprivatevault.rag.api.RagQueryScopeLimitException("Request body exceeds 64 KiB limit");
                }
            }
            return n;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }
    }
}
