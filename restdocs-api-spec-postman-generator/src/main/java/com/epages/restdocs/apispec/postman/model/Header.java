
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Header
 * <p>
 * Represents a single HTTP Header
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "value",
    "disabled",
    "description"
})
public class Header {

    /**
     * This holds the LHS of the HTTP Header, e.g ``Content-Type`` or ``X-Custom-Header``
     * (Required)
     * 
     */
    @JsonProperty("key")
    @JsonPropertyDescription("This holds the LHS of the HTTP Header, e.g ``Content-Type`` or ``X-Custom-Header``")
    private String key;
    /**
     * The value (or the RHS) of the Header is stored in this field.
     * (Required)
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The value (or the RHS) of the Header is stored in this field.")
    private String value;
    /**
     * If set to true, the current header will not be sent with requests.
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("If set to true, the current header will not be sent with requests.")
    private Boolean disabled = false;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;

    /**
     * This holds the LHS of the HTTP Header, e.g ``Content-Type`` or ``X-Custom-Header``
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * This holds the LHS of the HTTP Header, e.g ``Content-Type`` or ``X-Custom-Header``
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value (or the RHS) of the Header is stored in this field.
     * (Required)
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * The value (or the RHS) of the Header is stored in this field.
     * (Required)
     * 
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * If set to true, the current header will not be sent with requests.
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * If set to true, the current header will not be sent with requests.
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
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

}
