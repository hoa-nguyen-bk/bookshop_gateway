package com.example.bookshop_gateway.filter;


import com.example.bookshop_gateway.request.AuthenDecodeRequest;
import com.example.bookshop_gateway.response.AuthenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticationFilter extends OncePerRequestFilter {



    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String author = request.getHeader("Authorization");
        if (author != null && author.startsWith("Bearer ")) {

           String token = author.substring(7);
           HttpClient client = HttpClient.newHttpClient();

            AuthenDecodeRequest decodeRequest = new AuthenDecodeRequest();
            decodeRequest.setToken(token);
            ObjectMapper object = new ObjectMapper();
            String requestBody = object.writeValueAsString(decodeRequest);

               HttpRequest rqAuthen = HttpRequest.newBuilder()
                       .uri(URI.create("http://localhost:8081/authen/decode"))
                       .header("Content-Type", "application/json")
                       .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                       .build();
            try {
               HttpResponse<String> resqAuthen = client.send( rqAuthen, HttpResponse.BodyHandlers.ofString());
                AuthenResponse authenResponse = object.readValue(resqAuthen.body(), AuthenResponse.class);

                System.out.println("-----> authenResponse: "+ authenResponse.getMessage() + " - "+ authenResponse.getCode() +
                        " - "+ authenResponse.getData());
                List<GrantedAuthority> listAuthor = new ArrayList<>();
                for (String role : authenResponse.getData()) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    listAuthor.add(authority);
                }
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken("","", listAuthor));
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
        }
        // dù là sao cũng phải chạy tiếp
        filterChain.doFilter(request, response);
    }
}
