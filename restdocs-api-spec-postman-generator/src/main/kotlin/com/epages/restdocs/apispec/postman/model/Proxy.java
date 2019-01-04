
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Proxy Config
 * <p>
 * Using the Proxy, you can configure your custom proxy into the postman for particular url match
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "match",
    "host",
    "port",
    "tunnel",
    "disabled"
})
public class Proxy {

    /**
     * The Url match for which the proxy config is defined
     * 
     */
    @JsonProperty("match")
    @JsonPropertyDescription("The Url match for which the proxy config is defined")
    private String match = "http+https://*/*";
    /**
     * The proxy server host
     * 
     */
    @JsonProperty("host")
    @JsonPropertyDescription("The proxy server host")
    private String host;
    /**
     * The proxy server port
     * 
     */
    @JsonProperty("port")
    @JsonPropertyDescription("The proxy server port")
    private Integer port = 8080;
    /**
     * The tunneling details for the proxy config
     * 
     */
    @JsonProperty("tunnel")
    @JsonPropertyDescription("The tunneling details for the proxy config")
    private Boolean tunnel = false;
    /**
     * When set to true, ignores this proxy configuration entity
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("When set to true, ignores this proxy configuration entity")
    private Boolean disabled = false;

    /**
     * The Url match for which the proxy config is defined
     * 
     */
    @JsonProperty("match")
    public String getMatch() {
        return match;
    }

    /**
     * The Url match for which the proxy config is defined
     * 
     */
    @JsonProperty("match")
    public void setMatch(String match) {
        this.match = match;
    }

    /**
     * The proxy server host
     * 
     */
    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    /**
     * The proxy server host
     * 
     */
    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * The proxy server port
     * 
     */
    @JsonProperty("port")
    public Integer getPort() {
        return port;
    }

    /**
     * The proxy server port
     * 
     */
    @JsonProperty("port")
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The tunneling details for the proxy config
     * 
     */
    @JsonProperty("tunnel")
    public Boolean getTunnel() {
        return tunnel;
    }

    /**
     * The tunneling details for the proxy config
     * 
     */
    @JsonProperty("tunnel")
    public void setTunnel(Boolean tunnel) {
        this.tunnel = tunnel;
    }

    /**
     * When set to true, ignores this proxy configuration entity
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * When set to true, ignores this proxy configuration entity
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

}
