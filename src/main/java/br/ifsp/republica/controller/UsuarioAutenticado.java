package br.ifsp.republica.controller;

import org.springframework.security.oauth2.jwt.Jwt;

final class UsuarioAutenticado {

    private UsuarioAutenticado() {
    }

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
