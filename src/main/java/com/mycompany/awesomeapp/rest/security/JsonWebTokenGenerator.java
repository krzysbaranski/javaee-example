package com.mycompany.awesomeapp.rest.security;

import org.jboss.resteasy.jose.jwe.JWEBuilder;
import org.jboss.resteasy.jose.jwe.JWEInput;

import javax.json.Json;
import javax.json.JsonObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

public enum JsonWebTokenGenerator {

    INSTANCE;

    private KeyPair keyPair;

    private KeyPair key() throws Exception {
        if (keyPair == null) {
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        }
        return keyPair;
    }

    private String jsonWebToken(String username) throws Exception {

        long epochSecond = Instant.now().getEpochSecond() + 60 * 30 /* 30 min */;
        JsonObject model = Json.createObjectBuilder()
                .add("exp", epochSecond)
                .add("username", username)
                .build();
        return model.toString();
    }

    public String newToken(String username) throws Exception {

        String content = jsonWebToken(username);
        String encoded = new JWEBuilder().contentBytes(content.getBytes()).RSA1_5((RSAPublicKey) key().getPublic());
        return encoded;
}

    public String verifyJsonWebToken(String auth) throws Exception {
        if (null == auth) {
            //throw new Exception("Invalid JSON Web Token");
            return "null";
        }
        if (!auth.startsWith("Bearer ")) {
            //throw new Exception("Invalid JSON Web Token");
            return "null";
        }
        String encodedToken = auth.replaceFirst("Bearer ", "");

        byte[] raw = new JWEInput(encodedToken).decrypt((RSAPrivateKey) key().getPrivate()).getRawContent();
        String from = new String(raw);
        return from;

    }
}
