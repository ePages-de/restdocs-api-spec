
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Information
 * <p>
 * Detailed description of the info block
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "_postman_id",
    "description",
    "version",
    "schema"
})
public class Info {

    /**
     * Name of the collection
     * <p>
     * A collection's friendly name is defined by this field. You would want to set this field to a value that would allow you to easily identify this collection among a bunch of other collections, as such outlining its usage or content.
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("A collection's friendly name is defined by this field. You would want to set this field to a value that would allow you to easily identify this collection among a bunch of other collections, as such outlining its usage or content.")
    private String name;
    /**
     * Every collection is identified by the unique value of this field. The value of this field is usually easiest to generate using a UID generator function. If you already have a collection, it is recommended that you maintain the same id since changing the id usually implies that is a different collection than it was originally.
     *  *Note: This field exists for compatibility reasons with Collection Format V1.*
     * 
     */
    @JsonProperty("_postman_id")
    @JsonPropertyDescription("Every collection is identified by the unique value of this field. The value of this field is usually easiest to generate using a UID generator function. If you already have a collection, it is recommended that you maintain the same id since changing the id usually implies that is a different collection than it was originally.\n *Note: This field exists for compatibility reasons with Collection Format V1.*")
    private String postmanId;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;
    /**
     * Collection Version
     * <p>
     * Postman allows you to version your collections as they grow, and this field holds the version number. While optional, it is recommended that you use this field to its fullest extent!
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("Postman allows you to version your collections as they grow, and this field holds the version number. While optional, it is recommended that you use this field to its fullest extent!")
    private Object version;
    /**
     * This should ideally hold a link to the Postman schema that is used to validate this collection. E.g: https://schema.getpostman.com/collection/v1
     * (Required)
     * 
     */
    @JsonProperty("schema")
    @JsonPropertyDescription("This should ideally hold a link to the Postman schema that is used to validate this collection. E.g: https://schema.getpostman.com/collection/v1")
    private String schema;

    /**
     * Name of the collection
     * <p>
     * A collection's friendly name is defined by this field. You would want to set this field to a value that would allow you to easily identify this collection among a bunch of other collections, as such outlining its usage or content.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Name of the collection
     * <p>
     * A collection's friendly name is defined by this field. You would want to set this field to a value that would allow you to easily identify this collection among a bunch of other collections, as such outlining its usage or content.
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Every collection is identified by the unique value of this field. The value of this field is usually easiest to generate using a UID generator function. If you already have a collection, it is recommended that you maintain the same id since changing the id usually implies that is a different collection than it was originally.
     *  *Note: This field exists for compatibility reasons with Collection Format V1.*
     * 
     */
    @JsonProperty("_postman_id")
    public String getPostmanId() {
        return postmanId;
    }

    /**
     * Every collection is identified by the unique value of this field. The value of this field is usually easiest to generate using a UID generator function. If you already have a collection, it is recommended that you maintain the same id since changing the id usually implies that is a different collection than it was originally.
     *  *Note: This field exists for compatibility reasons with Collection Format V1.*
     * 
     */
    @JsonProperty("_postman_id")
    public void setPostmanId(String postmanId) {
        this.postmanId = postmanId;
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
     * Collection Version
     * <p>
     * Postman allows you to version your collections as they grow, and this field holds the version number. While optional, it is recommended that you use this field to its fullest extent!
     * 
     */
    @JsonProperty("version")
    public Object getVersion() {
        return version;
    }

    /**
     * Collection Version
     * <p>
     * Postman allows you to version your collections as they grow, and this field holds the version number. While optional, it is recommended that you use this field to its fullest extent!
     * 
     */
    @JsonProperty("version")
    public void setVersion(Object version) {
        this.version = version;
    }

    /**
     * This should ideally hold a link to the Postman schema that is used to validate this collection. E.g: https://schema.getpostman.com/collection/v1
     * (Required)
     * 
     */
    @JsonProperty("schema")
    public String getSchema() {
        return schema;
    }

    /**
     * This should ideally hold a link to the Postman schema that is used to validate this collection. E.g: https://schema.getpostman.com/collection/v1
     * (Required)
     * 
     */
    @JsonProperty("schema")
    public void setSchema(String schema) {
        this.schema = schema;
    }

}
