package com.epages.restdocs.apispec.postman.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 * FormData
 * <p>
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "type",
    "src",
    "description",

})
public class FormData {
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("key")
    private String key;
    /**
     * A Type can be text, or be file
     *
     */
    @JsonProperty("type")
    private String type;
    /**
     * The path to file, on the file system
     *
     */
    @JsonProperty("src")
    private List<String> src;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     *
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * A Type can be text, or be file
     *
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * A Type can be text, or be file
     *
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }
    /**
     * The path to file, on the file system
     *
     */
    @JsonProperty("src")
    public List<String> getSrc() {
        return src;
    }

    /**
     * The path to file, on the file system
     *
     */
    @JsonProperty("src")
    public void setSrc(List<String> src) {
        this.src = src;
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
