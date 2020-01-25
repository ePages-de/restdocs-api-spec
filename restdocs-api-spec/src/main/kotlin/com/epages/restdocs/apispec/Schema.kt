package com.epages.restdocs.apispec

/**
 * Represents a request/response object schema.
 */
data class Schema(val name: String) {

    companion object {
        fun schema(name: String): Schema = Schema(name)
    }
}