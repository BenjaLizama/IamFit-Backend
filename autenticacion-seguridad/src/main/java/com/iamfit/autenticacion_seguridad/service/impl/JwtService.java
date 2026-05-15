package com.iamfit.autenticacion_seguridad.service.impl;

import com.iamfit.autenticacion_seguridad.security.SecurityCredential;
import com.iamfit.autenticacion_seguridad.service.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.expiration.access-token:900000}") // 15 minutos por defecto en milisegundos.
    private Long jwtExpiration;

    /**
     * PERMITE QUE OTROS SERVICIOS PUEDAN OBTENER LA LLAVE PÚBLICA
     */
    @Override
    public String getPublicKeyAsBase64() {
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }

    /**
     * GENERA TOKEN FIRMADO CON LA LLAVE PRIVADA
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof SecurityCredential credential) {
            // Enviamos el UUID como String plano
            extraClaims.put("userId", credential.getId().toString());

            List<String> privileges = new ArrayList<>();
            List<String> roles = new ArrayList<>();

            credential.getAuthorities().forEach(authority -> {
                String auth = authority.getAuthority();
                if (auth.startsWith("ROLE_")) {
                    roles.add(auth);
                } else {
                    privileges.add(auth);
                }
            });

            extraClaims.put("roles", roles);
            extraClaims.put("privileges", privileges);
        }

        return Jwts.builder()
                .header().type("JWT").and()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(this.privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * EXTRAER EL USUARIO DEL TOKEN
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * VALIDAR TOKEN
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- MÉTODOS INTERNOS ---

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public long getRemainingTtlSeconds(String token) {
        Date expiration = extractExpiration(token);
        long remainingMs = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remainingMs / 1000);
    }


    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
