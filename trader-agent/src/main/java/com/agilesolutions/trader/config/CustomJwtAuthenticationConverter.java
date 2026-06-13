package com.agilesolutions.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom JWT Authentication Converter for Service B
 * 
 * Converts Keycloak JWT tokens into Spring Security authentication tokens.
 * Handles extraction of roles/scopes from JWT claims and maps them to GrantedAuthority.
 * 
 */
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Convert JWT to JwtAuthenticationToken with extracted authorities
     * 
     * @param jwt the JWT token to convert
     * @return JwtAuthenticationToken with name and authorities
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        log.debug("Converting JWT token to authentication token");
        
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = extractPrincipalName(jwt);
        
        log.debug("Extracted principal: {} with authorities count: {}", principalName, authorities.size());
        
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    /**
     * Extract authorities/roles from JWT token
     * 
     * Tries multiple claim paths for compatibility with different token issuers:
     * 1. resource_access.{clientId}.roles - Keycloak roles mapped to client
     * 2. scope - Space-separated scope claims
     * 3. authorities - Direct authorities claim
     * 
     * @param jwt the JWT token
     * @return collection of GrantedAuthorities
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        
        // Extract resource_access roles from Keycloak
        String clientId = extractClientId(jwt);
        if (clientId != null) {
            Collection<GrantedAuthority> resourceAccessRoles = extractResourceAccessRoles(jwt, clientId);
            authorities.addAll(resourceAccessRoles);
        }
        
        // Extract scope-based authorities
        Collection<GrantedAuthority> scopeAuthorities = extractScopeAuthorities(jwt);
        authorities.addAll(scopeAuthorities);
        
        return authorities;
    }

    /**
     * Extract resource_access roles for a specific client from Keycloak JWT
     * 
     * @param jwt the JWT token
     * @param clientId the Keycloak client ID
     * @return collection of role authorities
     */
    private Collection<GrantedAuthority> extractResourceAccessRoles(Jwt jwt, String clientId) {
        try {
            var resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
                var clientRoles = (java.util.Map<String, Object>) resourceAccess.get(clientId);
                if (clientRoles != null && clientRoles.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) clientRoles.get("roles");
                    if (roles != null) {
                        return roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract resource_access roles for client {}: {}", clientId, e.getMessage());
        }
        
        return List.of();
    }

    /**
     * Extract scope-based authorities from JWT
     * 
     * @param jwt the JWT token
     * @return collection of scope authorities
     */
    private Collection<GrantedAuthority> extractScopeAuthorities(Jwt jwt) {
        try {
            String scopes = jwt.getClaimAsString("scope");
            if (scopes != null && !scopes.isEmpty()) {
                return java.util.Arrays.stream(scopes.split(" "))
                        .filter(scope -> !scope.isEmpty())
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to extract scopes: {}", e.getMessage());
        }
        
        return List.of();
    }

    /**
     * Extract principal name (username) from JWT
     * 
     * Tries multiple claim paths for compatibility:
     * 1. sub (subject) - Standard JWT claim
     * 2. preferred_username - Keycloak specific
     * 3. clientId - Fallback to client ID
     * 
     * @param jwt the JWT token
     * @return principal name
     */
    private String extractPrincipalName(Jwt jwt) {
        // Try standard "sub" claim first
        if (jwt.getSubject() != null) {
            return jwt.getSubject();
        }
        
        // Try Keycloak "preferred_username"
        try {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null) {
                return preferredUsername;
            }
        } catch (Exception e) {
            log.debug("No preferred_username claim found");
        }
        
        // Fallback to client_id
        String clientId = extractClientId(jwt);
        if (clientId != null) {
            return clientId;
        }
        
        // Last resort: use token ID
        return jwt.getId();
    }

    /**
     * Extract client ID from JWT
     * 
     * Tries multiple claim paths:
     * 1. client_id - Standard OAuth2 claim
     * 2. clientId - Alternative format
     * 3. aud (audience) - Fallback to first audience
     * 
     * @param jwt the JWT token
     * @return client ID or null if not found
     */
    private String extractClientId(Jwt jwt) {
        try {
            // Try client_id claim
            String clientId = jwt.getClaimAsString("client_id");
            if (clientId != null) {
                return clientId;
            }
            
            // Try clientId claim
            String clientIdAlt = jwt.getClaimAsString("clientId");
            if (clientIdAlt != null) {
                return clientIdAlt;
            }
            
            // Try aud (audience) - first entry
            List<String> audience = jwt.getAudience();
            if (audience != null && !audience.isEmpty()) {
                return audience.get(0);
            }
        } catch (Exception e) {
            log.debug("Failed to extract client ID: {}", e.getMessage());
        }
        
        return null;
    }
}

