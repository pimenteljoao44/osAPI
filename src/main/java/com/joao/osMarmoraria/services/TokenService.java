package com.joao.osMarmoraria.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.joao.osMarmoraria.domain.Usuario;
import com.joao.osMarmoraria.services.exceptions.TokenInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario user){
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getLogin())
                    .withClaim("nivelAcesso", user.getNivelAcesso() != null ? user.getNivelAcesso().getDescricao() : null)
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    /**
     * Valida o token e devolve o login (subject) nele contido.
     *
     * @throws TokenInvalidoException se a assinatura, o emissor ou a
     *         expiração não passarem — a causa original (expirado, assinatura
     *         inválida etc.) fica preservada para o log do chamador.
     */
    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception){
            throw new TokenInvalidoException("Token JWT inválido", exception);
        }
    }

    private Instant genExpirationDate(){
        return LocalDateTime.now().plusHours(48).toInstant(ZoneOffset.of("-03:00"));
    }
}
