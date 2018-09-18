package com.epages.restdocs.openapi

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.operation.Operation

class SecurityRequirementsHandlerTest {

    private lateinit var operation: Operation
    private var securityRequirements: SecurityRequirements? = null

    private val securityRequirementsHandler = SecurityRequirementsHandler()

    @Test
    fun `should return basic security requirements`() {
        givenRequestWithBasicAuthHeader()

        whenSecurityRequirementsExtracted()

        then(securityRequirements).isEqualTo(Basic)
    }

    @Test
    fun `should return oauth security requirements`() {
        givenRequestWithJwtInAuthorizationHeader()

        whenSecurityRequirementsExtracted()

        then(securityRequirements).isEqualTo(Oauth2(listOf("scope1", "scope2")))
    }

    @Test
    fun `should return null when no requirements recognized`() {
        givenRequestWithoutAuthorizationHeader()

        whenSecurityRequirementsExtracted()

        then(securityRequirements).isNull()
    }

    private fun whenSecurityRequirementsExtracted() {
        securityRequirements = securityRequirementsHandler.extractSecurityRequirements(operation)
    }

    private fun givenRequestWithBasicAuthHeader() {
        operation = OperationBuilder().request("/some")
                .header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDpwd2QK")
                .build()
    }

    private fun givenRequestWithJwtInAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiZXhwIjoxNTA3NzU4NDk4LCJpYXQiOjE1MDc3MTUyOTgsImp0aSI6IjQyYTBhOTFhLWQ2ZWQtNDBjYy1iMTA2LWU5MGNkYWU0M2Q2ZCJ9.eWGo7Y124_Hdrr-bKX08d_oCfdgtlGXo9csz-hvRhRORJi_ZK7PIwM0ChqoLa4AhR-dJ86npid75GB9IxCW2f5E24FyZW2p5swpOpfkEAA4oFuj7jxHiaiqL_HFKKCRsVNAN3hGiSp9Hn3fde0-LlABqMaihdzZzHL-xm8-CqbXT-qBfuscDImZrZQZqhizpSEV4idbEMzZykggLASGoOIL0t0ycfe3yeuQkMUhzZmXuu08VM7zXwWnqfXCa-RmA6wC7ZnWqiJoi0vBr4BrlLR067YoUrT6pgRfiy2HZ0vEE_XY5SBtA-qI2QnlJb7eTk7pgFtoGkYdeOZ86k6GDVw"
                )
                .build()
    }

    private fun givenRequestWithoutAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
                .build()
    }
}
