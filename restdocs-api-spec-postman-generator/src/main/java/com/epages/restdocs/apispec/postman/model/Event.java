
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Event
 * <p>
 * Defines a script associated with an associated event name
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "listen",
    "script",
    "disabled"
})
public class Event {

    /**
     * A unique identifier for the enclosing event.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A unique identifier for the enclosing event.")
    private String id;
    /**
     * Can be set to `test` or `prerequest` for test scripts or pre-request scripts respectively.
     * (Required)
     * 
     */
    @JsonProperty("listen")
    @JsonPropertyDescription("Can be set to `test` or `prerequest` for test scripts or pre-request scripts respectively.")
    private String listen;
    /**
     * Script
     * <p>
     * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
     * 
     */
    @JsonProperty("script")
    @JsonPropertyDescription("A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.")
    private Script script;
    /**
     * Indicates whether the event is disabled. If absent, the event is assumed to be enabled.
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("Indicates whether the event is disabled. If absent, the event is assumed to be enabled.")
    private Boolean disabled = false;

    /**
     * A unique identifier for the enclosing event.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A unique identifier for the enclosing event.
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Can be set to `test` or `prerequest` for test scripts or pre-request scripts respectively.
     * (Required)
     * 
     */
    @JsonProperty("listen")
    public String getListen() {
        return listen;
    }

    /**
     * Can be set to `test` or `prerequest` for test scripts or pre-request scripts respectively.
     * (Required)
     * 
     */
    @JsonProperty("listen")
    public void setListen(String listen) {
        this.listen = listen;
    }

    /**
     * Script
     * <p>
     * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
     * 
     */
    @JsonProperty("script")
    public Script getScript() {
        return script;
    }

    /**
     * Script
     * <p>
     * A script is a snippet of Javascript code that can be used to to perform setup or teardown operations on a particular response.
     * 
     */
    @JsonProperty("script")
    public void setScript(Script script) {
        this.script = script;
    }

    /**
     * Indicates whether the event is disabled. If absent, the event is assumed to be enabled.
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * Indicates whether the event is disabled. If absent, the event is assumed to be enabled.
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

}
