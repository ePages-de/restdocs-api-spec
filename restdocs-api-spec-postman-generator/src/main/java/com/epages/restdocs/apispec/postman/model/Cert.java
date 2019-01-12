
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * An object containing path to file certificate, on the file system
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "src"
})
public class Cert {

    /**
     * The path to file containing key for certificate, on the file system
     * 
     */
    @JsonProperty("src")
    @JsonPropertyDescription("The path to file containing key for certificate, on the file system")
    private Object src;

    /**
     * The path to file containing key for certificate, on the file system
     * 
     */
    @JsonProperty("src")
    public Object getSrc() {
        return src;
    }

    /**
     * The path to file containing key for certificate, on the file system
     * 
     */
    @JsonProperty("src")
    public void setSrc(Object src) {
        this.src = src;
    }

}
