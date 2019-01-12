
package com.epages.restdocs.apispec.postman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;


/**
 * Response
 * <p>
 * A response represents an HTTP response.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "originalRequest",
    "responseTime",
    "header",
    "cookie",
    "body",
    "status",
    "code"
})
public class Response {

    /**
     * A unique, user defined identifier that can  be used to refer to this response from requests.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("A unique, user defined identifier that can  be used to refer to this response from requests.")
    private String id;
    @JsonProperty("name")
    private String name;
    /**
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * 
     */
    @JsonProperty("originalRequest")
    @JsonPropertyDescription("A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.")
    private Request originalRequest;
    /**
     * ResponseTime
     * <p>
     * The time taken by the request to complete. If a number, the unit is milliseconds. If the response is manually created, this can be set to `null`.
     * 
     */
    @JsonProperty("responseTime")
    @JsonPropertyDescription("The time taken by the request to complete. If a number, the unit is milliseconds. If the response is manually created, this can be set to `null`.")
    private Object responseTime;
    /**
     * Header
     * <p>
     * No HTTP request is complete without its headers, and the same is true for a Postman request. This field is an array containing all the headers.
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("No HTTP request is complete without its headers, and the same is true for a Postman request. This field is an array containing all the headers.")
    private List<Header> header = new ArrayList<Header>();
    @JsonProperty("cookie")
    private List<Cookie> cookie = new ArrayList<Cookie>();
    /**
     * The raw text of the response.
     * 
     */
    @JsonProperty("body")
    @JsonPropertyDescription("The raw text of the response.")
    private String body;
    /**
     * The response status, e.g: '200 OK'
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("The response status, e.g: '200 OK'")
    private String status;
    /**
     * The numerical response code, example: 200, 201, 404, etc.
     * 
     */
    @JsonProperty("code")
    @JsonPropertyDescription("The numerical response code, example: 200, 201, 404, etc.")
    private Integer code;

    /**
     * A unique, user defined identifier that can  be used to refer to this response from requests.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * A unique, user defined identifier that can  be used to refer to this response from requests.
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * 
     */
    @JsonProperty("originalRequest")
    public Request getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Request
     * <p>
     * A request represents an HTTP request. If a string, the string is assumed to be the request URL and the method is assumed to be 'GET'.
     * 
     */
    @JsonProperty("originalRequest")
    public void setOriginalRequest(Request originalRequest) {
        this.originalRequest = originalRequest;
    }

    /**
     * ResponseTime
     * <p>
     * The time taken by the request to complete. If a number, the unit is milliseconds. If the response is manually created, this can be set to `null`.
     * 
     */
    @JsonProperty("responseTime")
    public Object getResponseTime() {
        return responseTime;
    }

    /**
     * ResponseTime
     * <p>
     * The time taken by the request to complete. If a number, the unit is milliseconds. If the response is manually created, this can be set to `null`.
     * 
     */
    @JsonProperty("responseTime")
    public void setResponseTime(Object responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * Header
     * <p>
     * No HTTP request is complete without its headers, and the same is true for a Postman request. This field is an array containing all the headers.
     * 
     */
    @JsonProperty("header")
    public List<Header> getHeader() {
        return header;
    }

    /**
     * Header
     * <p>
     * No HTTP request is complete without its headers, and the same is true for a Postman request. This field is an array containing all the headers.
     * 
     */
    @JsonProperty("header")
    public void setHeader(List<Header> header) {
        this.header = header;
    }

    @JsonProperty("cookie")
    public List<Cookie> getCookie() {
        return cookie;
    }

    @JsonProperty("cookie")
    public void setCookie(List<Cookie> cookie) {
        this.cookie = cookie;
    }

    /**
     * The raw text of the response.
     * 
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    /**
     * The raw text of the response.
     * 
     */
    @JsonProperty("body")
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * The response status, e.g: '200 OK'
     * 
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * The response status, e.g: '200 OK'
     * 
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The numerical response code, example: 200, 201, 404, etc.
     * 
     */
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    /**
     * The numerical response code, example: 200, 201, 404, etc.
     * 
     */
    @JsonProperty("code")
    public void setCode(Integer code) {
        this.code = code;
    }

}
