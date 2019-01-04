
package com.epages.restdocs.apispec.postman.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Variable
 * <p>
 * Using variables in your Postman requests eliminates the need to duplicate requests, which can save a lot of time. Variables can be defined, and referenced to from any part of a request.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "key",
    "value",
    "type",
    "name",
    "description",
    "system",
    "disabled"
})
public class Variable {

    /**
     * A variable ID is a unique user-defined value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A variable ID is a unique user-defined value that identifies the variable within a collection. In traditional terms, this would be a variable name.")
    private String id;
    /**
     * A variable key is a human friendly value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("key")
    @JsonPropertyDescription("A variable key is a human friendly value that identifies the variable within a collection. In traditional terms, this would be a variable name.")
    private String key;
    /**
     * The value that a variable holds in this collection. Ultimately, the variables will be replaced by this value, when say running a set of requests from a collection
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The value that a variable holds in this collection. Ultimately, the variables will be replaced by this value, when say running a set of requests from a collection")
    private Object value;
    /**
     * A variable may have multiple types. This field specifies the type of the variable.
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("A variable may have multiple types. This field specifies the type of the variable.")
    private Variable.Type type;
    /**
     * Variable name
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Variable name")
    private String name;
    /**
     * A Description can be a raw text, or be an object, which holds the description along with its format.
     * 
     */
    @JsonProperty("description")
    @JsonPropertyDescription("A Description can be a raw text, or be an object, which holds the description along with its format.")
    private String description;
    /**
     * When set to true, indicates that this variable has been set by Postman
     * 
     */
    @JsonProperty("system")
    @JsonPropertyDescription("When set to true, indicates that this variable has been set by Postman")
    private Boolean system = false;
    @JsonProperty("disabled")
    private Boolean disabled = false;

    /**
     * A variable ID is a unique user-defined value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A variable ID is a unique user-defined value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * A variable key is a human friendly value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * A variable key is a human friendly value that identifies the variable within a collection. In traditional terms, this would be a variable name.
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value that a variable holds in this collection. Ultimately, the variables will be replaced by this value, when say running a set of requests from a collection
     * 
     */
    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    /**
     * The value that a variable holds in this collection. Ultimately, the variables will be replaced by this value, when say running a set of requests from a collection
     * 
     */
    @JsonProperty("value")
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * A variable may have multiple types. This field specifies the type of the variable.
     * 
     */
    @JsonProperty("type")
    public Variable.Type getType() {
        return type;
    }

    /**
     * A variable may have multiple types. This field specifies the type of the variable.
     * 
     */
    @JsonProperty("type")
    public void setType(Variable.Type type) {
        this.type = type;
    }

    /**
     * Variable name
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Variable name
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
     * When set to true, indicates that this variable has been set by Postman
     * 
     */
    @JsonProperty("system")
    public Boolean getSystem() {
        return system;
    }

    /**
     * When set to true, indicates that this variable has been set by Postman
     * 
     */
    @JsonProperty("system")
    public void setSystem(Boolean system) {
        this.system = system;
    }

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public enum Type {

        STRING("string"),
        BOOLEAN("boolean"),
        ANY("any"),
        NUMBER("number");
        private final String value;
        private final static Map<String, Variable.Type> CONSTANTS = new HashMap<String, Variable.Type>();

        static {
            for (Variable.Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Variable.Type fromValue(String value) {
            Variable.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
