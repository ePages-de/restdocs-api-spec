
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Script
 * <p>
 * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "type",
    "exec",
    "src",
    "name"
})
public class Script {

    /**
     * A unique, user defined identifier that can  be used to refer to this script from requests.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A unique, user defined identifier that can  be used to refer to this script from requests.")
    private String id;
    /**
     * Type of the script. E.g: 'text/javascript'
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Type of the script. E.g: 'text/javascript'")
    private String type;
    @JsonProperty("exec")
    private Object exec;
    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("src")
    @JsonPropertyDescription("If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.")
    private Src src;
    /**
     * Script name
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Script name")
    private String name;

    /**
     * A unique, user defined identifier that can  be used to refer to this script from requests.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A unique, user defined identifier that can  be used to refer to this script from requests.
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Type of the script. E.g: 'text/javascript'
     * 
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * Type of the script. E.g: 'text/javascript'
     * 
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("exec")
    public Object getExec() {
        return exec;
    }

    @JsonProperty("exec")
    public void setExec(Object exec) {
        this.exec = exec;
    }

    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("src")
    public Src getSrc() {
        return src;
    }

    /**
     * Url
     * <p>
     * If object, contains the complete broken-down URL for this request. If string, contains the literal request URL.
     * 
     */
    @JsonProperty("src")
    public void setSrc(Src src) {
        this.src = src;
    }

    /**
     * Script name
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Script name
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

}
