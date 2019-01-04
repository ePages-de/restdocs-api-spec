
package com.epages.restdocs.apispec.postman.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Certificate
 * <p>
 * A representation of an ssl certificate
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "matches",
    "key",
    "cert",
    "passphrase"
})
public class Certificate {

    /**
     * A name for the certificate for user reference
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("A name for the certificate for user reference")
    private String name;
    /**
     * A list of Url match pattern strings, to identify Urls this certificate can be used for.
     * 
     */
    @JsonProperty("matches")
    @JsonPropertyDescription("A list of Url match pattern strings, to identify Urls this certificate can be used for.")
    private List<Object> matches = new ArrayList<Object>();
    /**
     * An object containing path to file containing private key, on the file system
     * 
     */
    @JsonProperty("key")
    @JsonPropertyDescription("An object containing path to file containing private key, on the file system")
    private Key key;
    /**
     * An object containing path to file certificate, on the file system
     * 
     */
    @JsonProperty("cert")
    @JsonPropertyDescription("An object containing path to file certificate, on the file system")
    private Cert cert;
    /**
     * The passphrase for the certificate
     * 
     */
    @JsonProperty("passphrase")
    @JsonPropertyDescription("The passphrase for the certificate")
    private String passphrase;

    /**
     * A name for the certificate for user reference
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * A name for the certificate for user reference
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of Url match pattern strings, to identify Urls this certificate can be used for.
     * 
     */
    @JsonProperty("matches")
    public List<Object> getMatches() {
        return matches;
    }

    /**
     * A list of Url match pattern strings, to identify Urls this certificate can be used for.
     * 
     */
    @JsonProperty("matches")
    public void setMatches(List<Object> matches) {
        this.matches = matches;
    }

    /**
     * An object containing path to file containing private key, on the file system
     * 
     */
    @JsonProperty("key")
    public Key getKey() {
        return key;
    }

    /**
     * An object containing path to file containing private key, on the file system
     * 
     */
    @JsonProperty("key")
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * An object containing path to file certificate, on the file system
     * 
     */
    @JsonProperty("cert")
    public Cert getCert() {
        return cert;
    }

    /**
     * An object containing path to file certificate, on the file system
     * 
     */
    @JsonProperty("cert")
    public void setCert(Cert cert) {
        this.cert = cert;
    }

    /**
     * The passphrase for the certificate
     * 
     */
    @JsonProperty("passphrase")
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * The passphrase for the certificate
     * 
     */
    @JsonProperty("passphrase")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

}
