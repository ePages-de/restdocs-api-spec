
package com.epages.restdocs.apispec.postman.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Item
 * <p>
 * Items are entities which contain an actual HTTP request, and sample responses attached to it.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "description",
    "variable",
    "event",
    "request",
    "response",
    "protocolProfileBehavior"
})
public class Item {

    /**
     * A unique ID that is used to identify collections internally
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A unique ID that is used to identify collections internally")
    private String id;
    /**
     * A human readable identifier for the current item.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("A human readable identifier for the current item.")
    private String name;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;
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
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * (Required)
     * 
     */
    @JsonProperty("request")
    @JsonPropertyDescription("A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.")
    private Request request;
    /**
     * Responses
     * <p>
     * 
     * 
     */
    @JsonProperty("response")
    private List<Response> response = new ArrayList<Response>();
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
     * A unique ID that is used to identify collections internally
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A unique ID that is used to identify collections internally
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * A human readable identifier for the current item.
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * A human readable identifier for the current item.
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
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
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * (Required)
     * 
     */
    @JsonProperty("request")
    public Request getRequest() {
        return request;
    }

    /**
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * (Required)
     * 
     */
    @JsonProperty("request")
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Responses
     * <p>
     * 
     * 
     */
    @JsonProperty("response")
    public List<Response> getResponse() {
        return response;
    }

    /**
     * Responses
     * <p>
     * 
     * 
     */
    @JsonProperty("response")
    public void setResponse(List<Response> response) {
        this.response = response;
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
