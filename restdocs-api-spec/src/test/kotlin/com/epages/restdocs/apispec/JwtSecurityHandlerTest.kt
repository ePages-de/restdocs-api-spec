package com.epages.restdocs.apispec

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.restdocs.operation.Operation

class JwtSecurityHandlerTest {

    private val jwtSecurityHandler = JwtSecurityHandler()

    private var securityRequirement: SecurityRequirements? = null

    private lateinit var operation: Operation

    @Test
    fun `should add scope list when oauth2 jwt is found in Authorization header`() {
        givenRequestWithOAuth2JwtInAuthorizationHeader()

        whenSecurityRequirementsExtracted(operation)

        then(securityRequirement).isNotNull
        then((securityRequirement as Oauth2).requiredScopes).containsExactly("scope1", "scope2")
    }

    @Test
    fun `should return SecurityType of JWTBearer when non oauth2 jwt is found in Authorization header`() {
        givenRequestWithNonOAuth2JwtInAuthorizationHeader()

        whenSecurityRequirementsExtracted(operation)

        then(securityRequirement).isEqualTo(JWTBearer)
    }

    @Test
    fun should_do_nothing_when_authorization_header_missing() {
        givenRequestWithoutAuthorizationHeader()

        whenSecurityRequirementsExtracted(operation)

        then(securityRequirement).isNull()
    }

    @Test
    fun should_do_nothing_when_token_not_jwt() {
        givenRequestWithNonJwtInAuthorizationHeader()

        whenSecurityRequirementsExtracted(operation)

        then(securityRequirement).isNull()
    }

    @Test
    fun should_do_nothing_when_authorization_header_does_not_contain_jwt() {
        givenRequestWithBasicAuthHeader()

        whenSecurityRequirementsExtracted(operation)

        then(securityRequirement).isNull()
    }

    private fun whenSecurityRequirementsExtracted(operation: Operation) {
        securityRequirement = jwtSecurityHandler.extractSecurityRequirements(operation)
    }

    private fun givenRequestWithOAuth2JwtInAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
            .header(
                AUTHORIZATION,
                "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJzY29wZTEiLCJzY29wZTIiXSwiZXhwIjoxNTA3NzU4NDk4LCJpYXQiOjE1MDc3MTUyOTgsImp0aSI6IjQyYTBhOTFhLWQ2ZWQtNDBjYy1iMTA2LWU5MGNkYWU0M2Q2ZCJ9.eWGo7Y124_Hdrr-bKX08d_oCfdgtlGXo9csz-hvRhRORJi_ZK7PIwM0ChqoLa4AhR-dJ86npid75GB9IxCW2f5E24FyZW2p5swpOpfkEAA4oFuj7jxHiaiqL_HFKKCRsVNAN3hGiSp9Hn3fde0-LlABqMaihdzZzHL-xm8-CqbXT-qBfuscDImZrZQZqhizpSEV4idbEMzZykggLASGoOIL0t0ycfe3yeuQkMUhzZmXuu08VM7zXwWnqfXCa-RmA6wC7ZnWqiJoi0vBr4BrlLR067YoUrT6pgRfiy2HZ0vEE_XY5SBtA-qI2QnlJb7eTk7pgFtoGkYdeOZ86k6GDVw"
            )
            .build()
    }

    private fun givenRequestWithNonOAuth2JwtInAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
            .header(
                AUTHORIZATION,
                // this jwt token doesn't contain typ header but is still valid format
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.GuoUe6tw79bJlbU1HU0ADX0pr0u2kf3r_4OdrDufSfQ"
            )
            .build()
    }

    private fun givenRequestWithNonJwtInAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
            .header(AUTHORIZATION, "Bearer ey")
            .build()
    }

    private fun givenRequestWithoutAuthorizationHeader() {
        operation = OperationBuilder().request("/some")
            .build()
    }

    private fun givenRequestWithBasicAuthHeader() {
        operation = OperationBuilder().request("/some")
            .header(AUTHORIZATION, "Basic dGVzdDpwd2QK")
            .build()
    }
}
