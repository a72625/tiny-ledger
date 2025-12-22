package com.example.tiny_ledger.api.controller

import com.example.tiny_ledger.domain.exception.InsufficientFundsException
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.TransactionsResponse as DomainTransactionsResponse
import com.example.tiny_ledger.domain.service.LedgerService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.*

@WebMvcTest(controllers = [AccountController::class])
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: LedgerService

    @Test
    fun `should return 201 when opening account`() {
        val newId = UUID.randomUUID()
        whenever(service.openAccount()).thenReturn(AccountId(newId))

        mockMvc.perform(post("/v1/accounts"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accountId").value(newId.toString()))
    }

    @Test
    fun `should return balance for existing account`() {
        val accountId = UUID.randomUUID()
        whenever(service.getBalance(AccountId(accountId))).thenReturn(BigDecimal("150.50"))

        mockMvc.perform(get("/v1/accounts/$accountId/balances"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(150.50))
    }

    @Test
    fun `should return 202 when money movement is successful`() {
        val accountId = UUID.randomUUID()
        val json = """{ "amount": 100.0, "description": "Deposit" }"""

        mockMvc.perform(
            post("/v1/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isAccepted)
    }

    @Test
    fun `should return 422 when money movement fails due to insufficient funds`() {
        val accountId = UUID.randomUUID()

        // Use doThrow for Unit methods + anyOrNull for the nullable description
        doThrow(InsufficientFundsException())
            .whenever(service)
            .moneyMovement(any(), any(), anyOrNull()) // anyOrNull is key here

        mockMvc.perform(
            post("/v1/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{ "amount": -1000.0 }""")
        )
            .andExpect(status().isUnprocessableContent())
    }

    @Test
    fun `should return 400 when pagination size exceeds 100`() {
        val accountId = UUID.randomUUID()

        mockMvc.perform(
            get("/v1/accounts/$accountId/transactions")
                .param("size", "101")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return paged transactions with default values`() {
        val accountId = UUID.randomUUID()
        val domainResponse = DomainTransactionsResponse(
            transactions = emptyList(),
            page = 1,
            totalPages = 1,
            nextPage = null
        )

        whenever(service.getTransactions(any(), any())).thenReturn(domainResponse)

        mockMvc.perform(get("/v1/accounts/$accountId/transactions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.transactions").isArray)
    }

    @Test
    fun `should return 400 when description exceeds max length`() {
        val accountId = UUID.randomUUID()
        // Assuming your DTO/OpenAPI has a limit (e.g., 255 chars)
        val longDescription = "a".repeat(256)
        val json = """{ "amount": 10.0, "description": "$longDescription" }"""

        mockMvc.perform(post("/v1/accounts/$accountId/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when page number is 0`() {
        val accountId = UUID.randomUUID()

        // Triggers @Min(value = 1) on 'page' parameter
        mockMvc.perform(get("/v1/accounts/$accountId/transactions")
            .param("page", "0")
            .param("size", "10"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when page size is 0`() {
        val accountId = UUID.randomUUID()

        // Triggers @Min(value = 1) on 'size' parameter
        mockMvc.perform(get("/v1/accounts/$accountId/transactions")
            .param("page", "1")
            .param("size", "0"))
            .andExpect(status().isBadRequest)
    }
}