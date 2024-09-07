
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This field contains the data usually contained in the request body.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "mode",
    "raw",
    "urlencoded"
})
public class Body {

    /**
     * Postman stores the type of data associated with this request in this field.
     * 
     */
    @JsonProperty("mode")
    @JsonPropertyDescription("Postman stores the type of data associated with this request in this field.")
    private Body.Mode mode;
    @JsonProperty("raw")
    private String raw;
    @JsonProperty("urlencoded")
    private List<Urlencoded> urlencoded = new ArrayList<Urlencoded>();
    @JsonProperty("formdata")
    private List<FormData> formData = new ArrayList<FormData>();

    /**
     * Postman stores the type of data associated with this request in this field.
     * 
     */
    @JsonProperty("mode")
    public Body.Mode getMode() {
        return mode;
    }

    /**
     * Postman stores the type of data associated with this request in this field.
     * 
     */
    @JsonProperty("mode")
    public void setMode(Body.Mode mode) {
        this.mode = mode;
    }

    @JsonProperty("raw")
    public String getRaw() {
        return raw;
    }

    @JsonProperty("raw")
    public void setRaw(String raw) {
        this.raw = raw;
    }

    @JsonProperty("urlencoded")
    public List<Urlencoded> getUrlencoded() {
        return urlencoded;
    }

    @JsonProperty("urlencoded")
    public void setUrlencoded(List<Urlencoded> urlencoded) {
        this.urlencoded = urlencoded;
    }

    @JsonProperty("formdata")
    public List<FormData> getFormData() {
        return formData;
    }

    @JsonProperty("formdata")
    public void setFormData(List<FormData> formData) {
        this.formData = formData;
    }

    public enum Mode {

        RAW("raw"),
        URLENCODED("urlencoded"),
        FORMDATA("formdata"),
        FILE("file");
        private final String value;
        private final static Map<String, Body.Mode> CONSTANTS = new HashMap<String, Body.Mode>();

        static {
            for (Body.Mode c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Mode(String value) {
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
        public static Body.Mode fromValue(String value) {
            Body.Mode constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
