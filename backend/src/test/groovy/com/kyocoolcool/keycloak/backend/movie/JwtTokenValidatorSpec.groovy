package com.kyocoolcool.keycloak.backend.movie

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.kyocoolcool.keycloak.backend.infra.security.AccessToken
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Title

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAKeyGenParameterSpec
import java.time.Instant

@Title("Token validation rules")
@Subject(com.kyocoolcool.keycloak.backend.infra.security.JwtTokenValidator)
class JwtTokenValidatorSpec extends Specification {

    private com.kyocoolcool.keycloak.backend.infra.security.JwtTokenValidator validator
    private JwkProvider jwkProvider

    def setup() {
        jwkProvider = Stub(com.kyocoolcool.keycloak.backend.infra.security.config.KeycloakJwkProvider)
        validator = new com.kyocoolcool.keycloak.backend.infra.security.JwtTokenValidator(jwkProvider)
    }

    def "Create AccessToken from String value"() {

        given: "Generate RSA Key Pair"
        KeyPair keyPair = generateRsaKeyPair()
        stubJsonWebKey(keyPair)

        and: "Generate correct JWT Access token"
        def token = generateAccessToken(keyPair)

        when: "Validate access token"
        def accessToken = validator.validateAuthorizationHeader(AccessToken.BEARER + token)

        then: "AccessToken has been created"
        accessToken.valueAsString == token
    }

    def "AccessToken with incorrect signature"() {

        given: "Generate first RSA Key Pair"
        KeyPair firstKeyPair = generateRsaKeyPair()

        and: "Generate second RSA Key Pair"
        KeyPair secondKeyPair = generateRsaKeyPair()
        stubJsonWebKey(secondKeyPair)

        and: "Generate JWT Access token"
        def token = generateAccessToken(firstKeyPair)

        when: "Validate access token"
        validator.validateAuthorizationHeader(AccessToken.BEARER + token)

        then: "Token has invalid signature"
        def exception = thrown(com.kyocoolcool.keycloak.backend.infra.security.InvalidTokenException)
        exception.message == 'Token has invalid signature'
    }

    def "Expired Access Token"() {

        given: "Generate RSA Key Pair"
        KeyPair keyPair = generateRsaKeyPair()
        stubJsonWebKey(keyPair)

        and: "Generate JWT Access token"
        def jwtBuilder = JWT.create()
                .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))

        def token = generateAccessToken(keyPair, jwtBuilder)

        when: "Validate access token"
        validator.validateAuthorizationHeader(AccessToken.BEARER + token)

        then: "Token has invalid signature"
        def exception = thrown(com.kyocoolcool.keycloak.backend.infra.security.InvalidTokenException)
        exception.message == 'Token has expired'
    }

    def "Access Token without scope information"() {

        given: "Generate RSA Key Pair"
        KeyPair keyPair = generateRsaKeyPair()
        stubJsonWebKey(keyPair)

        and: "Generate JWT Access token"
        def jwtBuilder = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(5 * 60)))
                .withClaim("realm_access", Map.of("roles", List.of('ADMIN')))

        def token = generateAccessToken(keyPair, jwtBuilder)

        when: "Validate access token"
        validator.validateAuthorizationHeader(AccessToken.BEARER + token)

        then: "Token has invalid signature"
        def exception = thrown(com.kyocoolcool.keycloak.backend.infra.security.InvalidTokenException)
        exception.message == "Token doesn't contain scope information"
    }

    def "Access Token without user roles information"() {

        given: "Generate RSA Key Pair"
        KeyPair keyPair = generateRsaKeyPair()
        stubJsonWebKey(keyPair)

        and: "Generate JWT Access token"
        def jwtBuilder = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(5 * 60)))
                .withClaim("scope", List.of("openid"))


        def token = generateAccessToken(keyPair, jwtBuilder)

        when: "Validate access token"
        validator.validateAuthorizationHeader(AccessToken.BEARER + token)

        then: "Token has invalid signature"
        def exception = thrown(com.kyocoolcool.keycloak.backend.infra.security.InvalidTokenException)
        exception.message == "Token doesn't contain claims with realm roles"
    }

    private static KeyPair generateRsaKeyPair() {

        def keygen = KeyPairGenerator.getInstance("RSA")
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)
        keygen.initialize(spec)
        KeyPair keyPair = keygen.generateKeyPair()

        return keyPair
    }

    private void stubJsonWebKey(KeyPair keyPair) {
        def jwk = Stub(Jwk)
        jwk.getPublicKey() >> keyPair.getPublic()
        jwkProvider.get(_) >> jwk
    }

    private static String generateAccessToken(KeyPair keyPair) {

        Algorithm algorithm = Algorithm.RSA256(keyPair.getPublic() as RSAPublicKey, keyPair.getPrivate() as RSAPrivateKey)
        return JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(5 * 60)))
                .withClaim("scope", List.of("openid"))
                .withClaim("realm_access", Map.of("roles", List.of('ADMIN')))
                .sign(algorithm)
    }

    private static String generateAccessToken(KeyPair keyPair, JWTCreator.Builder builder) {

        Algorithm algorithm = Algorithm.RSA256(keyPair.getPublic() as RSAPublicKey, keyPair.getPrivate() as RSAPrivateKey)
        return builder.sign(algorithm)
    }
}
