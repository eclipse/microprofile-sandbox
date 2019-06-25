/*
 * Copyright (c) 2019 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.system.test.jupiter;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jose4j.base64url.SimplePEMEncoder;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;

/**
 * Build JWT's for use with Rest clients.
 * The public and private keys will be statically initialized and reused until this class goes away. 
 * 
 * @author brutif
 *
 */
public class JwtBuilder {
	 private static final String BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----";
	    private static final String END_PUBLIC_KEY = "-----END PUBLIC KEY-----";	    
	    private static final String DEFAULT_ISSUER="http://testissuer.com";	    
	JwtClaims claims = null;
    JsonWebSignature jws = null;
    static RsaJsonWebKey rsajwk = null;    
    static JwtBuilder me = null;    
    
    // init the single public:private key pair that we will re-use.
    private  static void init() {
    	if (rsajwk != null) {
    		return;
    	}
    	try {
    		rsajwk = RsaJwkGenerator.generateJwk(2048);
    		rsajwk.setKeyId("keyid");
    	} catch (Exception e) {
    		e.printStackTrace(System.out);
    	}
    }
    
	public static String getDefaultIssuer() {
		return DEFAULT_ISSUER;
	}
	
	public static String getPublicKey() {
		init();
		return pemEncode(rsajwk.getPublicKey());
	}
	
   private static String pemEncode(Key publicKey) {
        byte[] encoded = publicKey.getEncoded(); // X509 SPKI
        return BEGIN_PUBLIC_KEY + "\r\n" + SimplePEMEncoder.encode(encoded) + END_PUBLIC_KEY;
    }
	
	public static String buildJwt(String subject, String issuer, String[] claims ) {		
		me = new JwtBuilder();
		init();
		me.claims = new JwtClaims();
		me.jws = new JsonWebSignature();
					
		me.jws.setKeyIdHeaderValue(rsajwk.getKeyId());
		me.jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		// The JWT is signed using the private key, get the key we'll use every time.
	    me.jws.setKey(rsajwk.getPrivateKey()); 
	    if(subject != null) {
	    	me.claims.setClaim("sub", subject);
	    	me.claims.setClaim("upn", subject);
	    }
	    me.claims.setIssuer(DEFAULT_ISSUER);
	    me.claims.setExpirationTimeMinutesInTheFuture(60);
	    if (issuer != null) {
	    	me.claims.setIssuer(issuer);
	    }
	    setClaims(claims);
	    try {
			if (me.claims.getIssuedAt() == null) {
				me.claims.setIssuedAtToNow();
			}
		} catch (MalformedClaimException e1) {			
			e1.printStackTrace(System.out);
		}
    	me.jws.setPayload(me.claims.toJson());    	
    	try {
			return  me.jws.getCompactSerialization();			
		} catch (JoseException e) {			
			e.printStackTrace(System.out);
			return null;
		}   	
		
	}
	
	private static void setClaims(String[] claims) {
		for (String claim:claims) {
			if (!claim.contains("=")) continue;
			int loc = claim.indexOf('=');
			String claimName = claim.substring(0,loc);
			Object claimValue = claim.substring(loc+1);
			claimValue = handleArrays((String)claimValue);
			setClaim(claimName, claimValue);
		}
	}
	
	private static Object handleArrays(String claimValue) {
		if (! claimValue.contains(",")) {
			return claimValue;
		}
		String[] elements = claimValue.split(",");
		return elements;
	}
	
	public static void main(String[] args) {
		String[] sa = (String[]) handleArrays(",fred,bob,barney,");
		for(int i=0; i<sa.length; i++) {
			System.out.println(sa[i]);
		}
		System.out.println("end");
		
	}
	
	private static void setClaim(String name, Object value) {
	    System.out.println(name + " " + value);		    
	    me.claims.setClaim(name, value);
	}
}
