
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;


/**
 * Url
 * <p>
 * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "raw",
    "protocol",
    "host",
    "path",
    "port",
    "query",
    "hash",
    "variable"
})
public class Src {

    /**
     * The string representation of the request URL, including the protocol, host, path, hash, query parameter(s) and path variable(s).
     * 
     */
    @JsonProperty("raw")
    @JsonPropertyDescription("The string representation of the request URL, including the protocol, host, path, hash, query parameter(s) and path variable(s).")
    private String raw;
    /**
     * The protocol associated with the request, E.g: 'http'
     * 
     */
    @JsonProperty("protocol")
    @JsonPropertyDescription("The protocol associated with the request, E.g: 'http'")
    private String protocol;
    /**
     * Host
     * <p>
     * The host for the URL, E.g: api.yourdomain.com. Can be stored as a string or as an array of strings.
     * 
     */
    @JsonProperty("host")
    @JsonPropertyDescription("The host for the URL, E.g: api.yourdomain.com. Can be stored as a string or as an array of strings.")
    private Object host;
    @JsonProperty("path")
    private String path;
    /**
     * The port number present in this URL. An empty value implies 80/443 depending on whether the protocol field contains http/https.
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("The port number present in this URL. An empty value implies 80/443 depending on whether the protocol field contains http/https.")
    private String port;
    /**
     * An array of QueryParams, which is basically the query string part of the URL, parsed into separate variables
     * 
     */
    @JsonProperty("query")
    @JsonPropertyDescription("An array of QueryParams, which is basically the query string part of the URL, parsed into separate variables")
    private List<Query> query = new ArrayList<Query>();
    /**
     * Contains the URL fragment (if any). Usually this is not transmitted over the network, but it could be useful to store this in some cases.
     * 
     */
    @JsonProperty("hash")
    @JsonPropertyDescription("Contains the URL fragment (if any). Usually this is not transmitted over the network, but it could be useful to store this in some cases.")
    private String hash;
    /**
     * Postman supports path variables with the syntax `/path/:variableName/to/somewhere`. These variables are stored in this field.
     * 
     */
    @JsonProperty("variable")
    @JsonPropertyDescription("Postman supports path variables with the syntax `/path/:variableName/to/somewhere`. These variables are stored in this field.")
    private List<Variable> variable = new ArrayList<Variable>();

    /**
     * The string representation of the request URL, including the protocol, host, path, hash, query parameter(s) and path variable(s).
     * 
     */
    @JsonProperty("raw")
    public String getRaw() {
        return raw;
    }

    /**
     * The string representation of the request URL, including the protocol, host, path, hash, query parameter(s) and path variable(s).
     * 
     */
    @JsonProperty("raw")
    public void setRaw(String raw) {
        this.raw = raw;
    }

    /**
     * The protocol associated with the request, E.g: 'http'
     * 
     */
    @JsonProperty("protocol")
    public String getProtocol() {
        return protocol;
    }

    /**
     * The protocol associated with the request, E.g: 'http'
     * 
     */
    @JsonProperty("protocol")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Host
     * <p>
     * The host for the URL, E.g: api.yourdomain.com. Can be stored as a string or as an array of strings.
     * 
     */
    @JsonProperty("host")
    public Object getHost() {
        return host;
    }

    /**
     * Host
     * <p>
     * The host for the URL, E.g: api.yourdomain.com. Can be stored as a string or as an array of strings.
     * 
     */
    @JsonProperty("host")
    public void setHost(Object host) {
        this.host = host;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The port number present in this URL. An empty value implies 80/443 depending on whether the protocol field contains http/https.
     * 
     */
    @JsonProperty("port")
    public String getPort() {
        return port;
    }

    /**
     * The port number present in this URL. An empty value implies 80/443 depending on whether the protocol field contains http/https.
     * 
     */
    @JsonProperty("port")
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * An array of QueryParams, which is basically the query string part of the URL, parsed into separate variables
     * 
     */
    @JsonProperty("query")
    public List<Query> getQuery() {
        return query;
    }

    /**
     * An array of QueryParams, which is basically the query string part of the URL, parsed into separate variables
     * 
     */
    @JsonProperty("query")
    public void setQuery(List<Query> query) {
        this.query = query;
    }

    /**
     * Contains the URL fragment (if any). Usually this is not transmitted over the network, but it could be useful to store this in some cases.
     * 
     */
    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    /**
     * Contains the URL fragment (if any). Usually this is not transmitted over the network, but it could be useful to store this in some cases.
     * 
     */
    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Postman supports path variables with the syntax `/path/:variableName/to/somewhere`. These variables are stored in this field.
     * 
     */
    @JsonProperty("variable")
    public List<Variable> getVariable() {
        return variable;
    }

    /**
     * Postman supports path variables with the syntax `/path/:variableName/to/somewhere`. These variables are stored in this field.
     * 
     */
    @JsonProperty("variable")
    public void setVariable(List<Variable> variable) {
        this.variable = variable;
    }

}
