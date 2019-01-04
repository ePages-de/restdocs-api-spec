
package com.epages.restdocs.apispec.postman.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Request
 * <p>
 * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "url",
    "auth",
    "proxy",
    "certificate",
    "method",
    "description",
    "header",
    "body"
})
public class Request {

    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.")
    private Src url;
    @JsonProperty("auth")
    private Object auth;
    /**
     * Proxy Config
     * <p>
     * Using the Proxy, you can configure your custom proxy into the postman for particular url match
     * 
     */
    @JsonProperty("proxy")
    @JsonPropertyDescription("Using the Proxy, you can configure your custom proxy into the postman for particular url match")
    private Proxy proxy;
    /**
     * Certificate
     * <p>
     * A representation of an ssl certificate
     * 
     */
    @JsonProperty("certificate")
    @JsonPropertyDescription("A representation of an ssl certificate")
    private Certificate certificate;
    @JsonProperty("method")
    private Object method;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;
    /**
     * Header List
     * <p>
     * A representation for a list of headers
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("A representation for a list of headers")
    private List<Header> header = new ArrayList<Header>();
    /**
     * This field contains the data usually contained in the request body.
     * 
     */
    @JsonProperty("body")
    @JsonPropertyDescription("This field contains the data usually contained in the request body.")
    private Body body;

    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("url")
    public Src getUrl() {
        return url;
    }

    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("url")
    public void setUrl(Src url) {
        this.url = url;
    }

    @JsonProperty("auth")
    public Object getAuth() {
        return auth;
    }

    @JsonProperty("auth")
    public void setAuth(Object auth) {
        this.auth = auth;
    }

    /**
     * Proxy Config
     * <p>
     * Using the Proxy, you can configure your custom proxy into the postman for particular url match
     * 
     */
    @JsonProperty("proxy")
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Proxy Config
     * <p>
     * Using the Proxy, you can configure your custom proxy into the postman for particular url match
     * 
     */
    @JsonProperty("proxy")
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Certificate
     * <p>
     * A representation of an ssl certificate
     * 
     */
    @JsonProperty("certificate")
    public Certificate getCertificate() {
        return certificate;
    }

    /**
     * Certificate
     * <p>
     * A representation of an ssl certificate
     * 
     */
    @JsonProperty("certificate")
    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    @JsonProperty("method")
    public Object getMethod() {
        return method;
    }

    @JsonProperty("method")
    public void setMethod(Object method) {
        this.method = method;
    }

    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Header List
     * <p>
     * A representation for a list of headers
     * 
     */
    @JsonProperty("header")
    public List<Header> getHeader() {
        return header;
    }

    /**
     * Header List
     * <p>
     * A representation for a list of headers
     * 
     */
    @JsonProperty("header")
    public void setHeader(List<Header> header) {
        this.header = header;
    }

    /**
     * This field contains the data usually contained in the request body.
     * 
     */
    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    /**
     * This field contains the data usually contained in the request body.
     * 
     */
    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

}
