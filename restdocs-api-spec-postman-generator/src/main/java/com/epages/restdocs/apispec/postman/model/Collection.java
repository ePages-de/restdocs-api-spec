
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "info",
    "item",
    "event",
    "variable",
    "auth",
    "protocolProfileBehavior"
})
public class Collection {

    /**
     * Information
     * <p>
     * Detailed description of the info block
     * (Required)
     * 
     */
    @JsonProperty("info")
    @JsonPropertyDescription("Detailed description of the info block")
    private Info info;
    /**
     * Items are the basic unit for a Postman collection. You can think of them as corresponding to a single API endpoint. Each Item has one request and may have multiple API responses associated with it.
     * (Required)
     * 
     */
    @JsonProperty("item")
    @JsonPropertyDescription("Items are the basic unit for a Postman collection. You can think of them as corresponding to a single API endpoint. Each Item has one request and may have multiple API responses associated with it.")
    private List<Item> item = new ArrayList<Item>();
    /**
     * Event List
     * <p>
     * Postman allows you to configure scripts to run when specific events occur. These scripts are stored here, and can be referenced in the collection by their ID.
     * 
     */
    @JsonProperty("event")
    @JsonPropertyDescription("Postman allows you to configure scripts to run when specific events occur. These scripts are stored here, and can be referenced in the collection by their ID.")
    private List<Event> event = new ArrayList<Event>();
    /**
     * Variable List
     * <p>
     * Collection variables allow you to define a set of variables, that are a *part of the collection*, as opposed to environments, which are separate entities.
     * *Note: Collection variables must not contain any sensitive information.*
     * 
     */
    @JsonProperty("variable")
    @JsonPropertyDescription("Collection variables allow you to define a set of variables, that are a *part of the collection*, as opposed to environments, which are separate entities.\n*Note: Collection variables must not contain any sensitive information.*")
    private List<Variable> variable = new ArrayList<Variable>();
    @JsonProperty("auth")
    private Object auth;
    /**
     * Protocol Profile Behavior
     * <p>
     * Set of configurations used to alter the usual behavior of sending the request
     * 
     */
    @JsonProperty("protocolProfileBehavior")
    @JsonPropertyDescription("Set of configurations used to alter the usual behavior of sending the request")
    private ProtocolProfileBehavior protocolProfileBehavior;

    /**
     * Information
     * <p>
     * Detailed description of the info block
     * (Required)
     * 
     */
    @JsonProperty("info")
    public Info getInfo() {
        return info;
    }

    /**
     * Information
     * <p>
     * Detailed description of the info block
     * (Required)
     * 
     */
    @JsonProperty("info")
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * Items are the basic unit for a Postman collection. You can think of them as corresponding to a single API endpoint. Each Item has one request and may have multiple API responses associated with it.
     * (Required)
     * 
     */
    @JsonProperty("item")
    public List<Item> getItem() {
        return item;
    }

    /**
     * Items are the basic unit for a Postman collection. You can think of them as corresponding to a single API endpoint. Each Item has one request and may have multiple API responses associated with it.
     * (Required)
     * 
     */
    @JsonProperty("item")
    public void setItem(List<Item> item) {
        this.item = item;
    }

    /**
     * Event List
     * <p>
     * Postman allows you to configure scripts to run when specific events occur. These scripts are stored here, and can be referenced in the collection by their ID.
     * 
     */
    @JsonProperty("event")
    public List<Event> getEvent() {
        return event;
    }

    /**
     * Event List
     * <p>
     * Postman allows you to configure scripts to run when specific events occur. These scripts are stored here, and can be referenced in the collection by their ID.
     * 
     */
    @JsonProperty("event")
    public void setEvent(List<Event> event) {
        this.event = event;
    }

    /**
     * Variable List
     * <p>
     * Collection variables allow you to define a set of variables, that are a *part of the collection*, as opposed to environments, which are separate entities.
     * *Note: Collection variables must not contain any sensitive information.*
     * 
     */
    @JsonProperty("variable")
    public List<Variable> getVariable() {
        return variable;
    }

    /**
     * Variable List
     * <p>
     * Collection variables allow you to define a set of variables, that are a *part of the collection*, as opposed to environments, which are separate entities.
     * *Note: Collection variables must not contain any sensitive information.*
     * 
     */
    @JsonProperty("variable")
    public void setVariable(List<Variable> variable) {
        this.variable = variable;
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
     * Protocol Profile Behavior
     * <p>
     * Set of configurations used to alter the usual behavior of sending the request
     * 
     */
    @JsonProperty("protocolProfileBehavior")
    public ProtocolProfileBehavior getProtocolProfileBehavior() {
        return protocolProfileBehavior;
    }

    /**
     * Protocol Profile Behavior
     * <p>
     * Set of configurations used to alter the usual behavior of sending the request
     * 
     */
    @JsonProperty("protocolProfileBehavior")
    public void setProtocolProfileBehavior(ProtocolProfileBehavior protocolProfileBehavior) {
        this.protocolProfileBehavior = protocolProfileBehavior;
    }

}
