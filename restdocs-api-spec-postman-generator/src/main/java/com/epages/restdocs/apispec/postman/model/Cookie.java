
package com.epages.restdocs.apispec.postman.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Cookie
 * <p>
 * A Cookie, that follows the [Google Chrome format](https://developer.chrome.com/extensions/cookies)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "domain",
    "expires",
    "maxAge",
    "hostOnly",
    "httpOnly",
    "name",
    "path",
    "secure",
    "session",
    "value",
    "extensions"
})
public class Cookie {

    /**
     * The domain for which this cookie is valid.
     * (Required)
     * 
     */
    @JsonProperty("domain")
    @JsonPropertyDescription("The domain for which this cookie is valid.")
    private String domain;
    /**
     * When the cookie expires.
     * 
     */
    @JsonProperty("expires")
    @JsonPropertyDescription("When the cookie expires.")
    private Object expires;
    @JsonProperty("maxAge")
    private String maxAge;
    /**
     * True if the cookie is a host-only cookie. (i.e. a request's URL domain must exactly match the domain of the cookie).
     * 
     */
    @JsonProperty("hostOnly")
    @JsonPropertyDescription("True if the cookie is a host-only cookie. (i.e. a request's URL domain must exactly match the domain of the cookie).")
    private Boolean hostOnly;
    /**
     * Indicates if this cookie is HTTP Only. (if True, the cookie is inaccessible to client-side scripts)
     * 
     */
    @JsonProperty("httpOnly")
    @JsonPropertyDescription("Indicates if this cookie is HTTP Only. (if True, the cookie is inaccessible to client-side scripts)")
    private Boolean httpOnly;
    /**
     * This is the name of the Cookie.
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("This is the name of the Cookie.")
    private String name;
    /**
     * The path associated with the Cookie.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("The path associated with the Cookie.")
    private String path;
    /**
     * Indicates if the 'secure' flag is set on the Cookie, meaning that it is transmitted over secure connections only. (typically HTTPS)
     * 
     */
    @JsonProperty("secure")
    @JsonPropertyDescription("Indicates if the 'secure' flag is set on the Cookie, meaning that it is transmitted over secure connections only. (typically HTTPS)")
    private Boolean secure;
    /**
     * True if the cookie is a session cookie.
     * 
     */
    @JsonProperty("session")
    @JsonPropertyDescription("True if the cookie is a session cookie.")
    private Boolean session;
    /**
     * The value of the Cookie.
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The value of the Cookie.")
    private String value;
    /**
     * Custom attributes for a cookie go here, such as the [Priority Field](https://code.google.com/p/chromium/issues/detail?id=232693)
     * 
     */
    @JsonProperty("extensions")
    @JsonPropertyDescription("Custom attributes for a cookie go here, such as the [Priority Field](https://code.google.com/p/chromium/issues/detail?id=232693)")
    private List<Object> extensions = new ArrayList<Object>();

    /**
     * The domain for which this cookie is valid.
     * (Required)
     * 
     */
    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    /**
     * The domain for which this cookie is valid.
     * (Required)
     * 
     */
    @JsonProperty("domain")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * When the cookie expires.
     * 
     */
    @JsonProperty("expires")
    public Object getExpires() {
        return expires;
    }

    /**
     * When the cookie expires.
     * 
     */
    @JsonProperty("expires")
    public void setExpires(Object expires) {
        this.expires = expires;
    }

    @JsonProperty("maxAge")
    public String getMaxAge() {
        return maxAge;
    }

    @JsonProperty("maxAge")
    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * True if the cookie is a host-only cookie. (i.e. a request's URL domain must exactly match the domain of the cookie).
     * 
     */
    @JsonProperty("hostOnly")
    public Boolean getHostOnly() {
        return hostOnly;
    }

    /**
     * True if the cookie is a host-only cookie. (i.e. a request's URL domain must exactly match the domain of the cookie).
     * 
     */
    @JsonProperty("hostOnly")
    public void setHostOnly(Boolean hostOnly) {
        this.hostOnly = hostOnly;
    }

    /**
     * Indicates if this cookie is HTTP Only. (if True, the cookie is inaccessible to client-side scripts)
     * 
     */
    @JsonProperty("httpOnly")
    public Boolean getHttpOnly() {
        return httpOnly;
    }

    /**
     * Indicates if this cookie is HTTP Only. (if True, the cookie is inaccessible to client-side scripts)
     * 
     */
    @JsonProperty("httpOnly")
    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * This is the name of the Cookie.
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * This is the name of the Cookie.
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The path associated with the Cookie.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * The path associated with the Cookie.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Indicates if the 'secure' flag is set on the Cookie, meaning that it is transmitted over secure connections only. (typically HTTPS)
     * 
     */
    @JsonProperty("secure")
    public Boolean getSecure() {
        return secure;
    }

    /**
     * Indicates if the 'secure' flag is set on the Cookie, meaning that it is transmitted over secure connections only. (typically HTTPS)
     * 
     */
    @JsonProperty("secure")
    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    /**
     * True if the cookie is a session cookie.
     * 
     */
    @JsonProperty("session")
    public Boolean getSession() {
        return session;
    }

    /**
     * True if the cookie is a session cookie.
     * 
     */
    @JsonProperty("session")
    public void setSession(Boolean session) {
        this.session = session;
    }

    /**
     * The value of the Cookie.
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * The value of the Cookie.
     * 
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Custom attributes for a cookie go here, such as the [Priority Field](https://code.google.com/p/chromium/issues/detail?id=232693)
     * 
     */
    @JsonProperty("extensions")
    public List<Object> getExtensions() {
        return extensions;
    }

    /**
     * Custom attributes for a cookie go here, such as the [Priority Field](https://code.google.com/p/chromium/issues/detail?id=232693)
     * 
     */
    @JsonProperty("extensions")
    public void setExtensions(List<Object> extensions) {
        this.extensions = extensions;
    }

}
