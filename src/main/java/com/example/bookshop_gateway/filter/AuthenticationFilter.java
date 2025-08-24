package com.example.bookshop_gateway.filter;

import com.example.bookshop_gateway.dto.AuthenRequest;
import com.example.bookshop_gateway.response.AuthenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Service
public class AuthenticationFilter extends OncePerRequestFilter {

    CloseableHttpClient httpClient;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // gọi decode giải mã token, call api qua service authen để gọi ra kết quả
        CloseableHttpClient httpClient = HttpClients.custom().build();

        String url = "http://localhost:8081/authen/decode";

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");

        String headerAuthor = request.getHeader("Authorization");
        if (headerAuthor != null && headerAuthor.startsWith("Bearer ")) {
            String token = headerAuthor.substring(7);
            AuthenRequest authenRequest = new AuthenRequest();
            authenRequest.setToken(token);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(authenRequest);

            httpPost.setEntity(EntityBuilder.create()
                .setText(json)
                .setContentType(ContentType.APPLICATION_JSON)
                .build());

            //respAuthen đại diện kết quả mình trả ra
            try (CloseableHttpResponse respAuthen = httpClient.execute(httpPost)) {
                if(respAuthen.getCode() != 200) {
                    throw new RuntimeException("Authentication Failed" + respAuthen.getCode());
                }
                String jsonResponse = EntityUtils.toString(respAuthen.getEntity());
                AuthenResponse authenResponse = objectMapper.readValue(jsonResponse, AuthenResponse.class);

                SecurityContext securityContext = SecurityContextHolder.getContext();

                List<SimpleGrantedAuthority> authorities = authenResponse.getData().stream()
                        .map(SimpleGrantedAuthority::new).toList();

                UsernamePasswordAuthenticationToken authenToken = new UsernamePasswordAuthenticationToken(
                        "","",authorities
                );

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }

        // dù là sao cũng phải chạy tiếp
        filterChain.doFilter(request, response);
    }
}
