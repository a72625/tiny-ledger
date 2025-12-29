package com.example.tiny_ledger.domain.service

import com.example.tiny_ledger.domain.data.LedgerRepository
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Currency
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.Sort
import com.example.tiny_ledger.domain.model.TransactionsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.*
import kotlin.collections.emptyList

class LedgerServiceTest {

    private val repository: LedgerRepository = mock()
    private val service = LedgerService(repository, CurrencyConfig(mutableMapOf("EUR" to 2)))

    @Test
    fun `openAccount should generate UUID and call repository`() {
        val result = service.openAccount(Currency.EUR)

        // Verify it's a valid AccountId (UUID)
        assert(result.id is UUID)
        // Verify repository was called exactly once with that ID
        verify(repository, times(1)).openAccount(
            result,
            Currency.EUR
        )
    }

    @Test
    fun `moneyMovement should throw exception when amount is zero`() {
        val accountId = AccountId(UUID.randomUUID())

        val exception = assertThrows<IllegalArgumentException> {
            service.moneyMovement(accountId, BigDecimal("0.00"), "Zero test")
        }

        assertEquals("Amount cannot be zero", exception.message)
        verifyNoInteractions(repository)
    }

    @Test
    fun `moneyMovement should throw exception when precision is too high`() {
        val accountId = AccountId(UUID.randomUUID())
        whenever(repository.getAccountCurrency(accountId)).thenReturn(Currency.EUR)
        // Scale is 9, MAXIMUM_PRECISION is 8
        val highPrecisionAmount = BigDecimal("-10.123456789")

        val exception = assertThrows<IllegalArgumentException> {
            service.moneyMovement(accountId, highPrecisionAmount, "Precision test")
        }

        assertEquals("Precision too high", exception.message)
        verify(repository, times(0)).storeTransaction(any(), any(), any())
    }

    @Test
    fun `moneyMovement should call repository when validation passes`() {
        val accountId = AccountId(UUID.randomUUID())
        whenever(repository.getAccountCurrency(accountId)).thenReturn(Currency.EUR)
        val validAmount = BigDecimal("10.50")

        service.moneyMovement(accountId, validAmount, "Valid deposit")

        verify(repository).storeTransaction(accountId, validAmount, "Valid deposit")
    }

    @Test
    fun `getBalance should delegate to repository`() {
        val accountId = AccountId(UUID.randomUUID())
        val expectedBalance = BigDecimal("100.00")
        whenever(repository.getBalance(accountId)).thenReturn(expectedBalance)

        val actualBalance = service.getBalance(accountId)

        assertEquals(expectedBalance, actualBalance)
        verify(repository).getBalance(accountId)
    }

    @Test
    fun `getTransactions should delegate to repository`() {
        val accountId = AccountId(UUID.randomUUID())
        val pageable = Pageable(1, 10, Sort.DESC)
        val mockResponse = TransactionsResponse(page = 1, nextPage = 1, totalPages = 1, transactions = emptyList())

        whenever(repository.getTransactions(accountId, pageable)).thenReturn(mockResponse)

        val result = service.getTransactions(accountId, pageable)

        assertEquals(mockResponse, result)
        verify(repository).getTransactions(accountId, pageable)
    }
}