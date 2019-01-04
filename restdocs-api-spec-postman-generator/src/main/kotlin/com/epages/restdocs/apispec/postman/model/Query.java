
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * QueryParam
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "value",
    "disabled",
    "description"
})
public class Query {

    @JsonProperty("key")
    private Object key;
    @JsonProperty("value")
    private Object value;
    /**
     * If set to true, the current query parameter will not be sent with the request.
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("If set to true, the current query parameter will not be sent with the request.")
    private Boolean disabled = false;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;

    @JsonProperty("key")
    public Object getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(Object key) {
        this.key = key;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * If set to true, the current query parameter will not be sent with the request.
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * If set to true, the current query parameter will not be sent with the request.
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
