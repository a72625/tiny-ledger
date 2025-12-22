package com.example.tiny_ledger.domain.repository

import com.example.tiny_ledger.domain.data.LedgerRepository
import com.example.tiny_ledger.domain.exception.InsufficientFundsException
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.Sort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.UUID

class LedgerRepositoryTest {

    private lateinit var repository: LedgerRepository
    private val accountId = AccountId(UUID.randomUUID())

    @BeforeEach
    fun setup() {
        repository = LedgerRepository()
        repository.openAccount(accountId)
    }

    @Test
    fun `getBalance should return zero for new account`() {
        assertEquals(BigDecimal.ZERO, repository.getBalance(accountId))
    }

    @Test
    fun `storeTransaction should update balance correctly`() {
        repository.storeTransaction(accountId, BigDecimal("100.00"), "Deposit")
        repository.storeTransaction(accountId, BigDecimal("-40.50"), "Withdrawal")

        assertEquals(BigDecimal("59.50"), repository.getBalance(accountId))
    }

    @Test
    fun `storeTransaction should throw InsufficientFundsException when balance goes negative`() {
        repository.storeTransaction(accountId, BigDecimal("50.00"), "Initial")

        assertThrows<InsufficientFundsException> {
            repository.storeTransaction(accountId, BigDecimal("-50.01"), "Too much")
        }

        // Ensure balance stayed at 50
        assertEquals(BigDecimal("50.00"), repository.getBalance(accountId))
    }

    @Test
    fun `getTransactions should return transactions in natural DESC order (newest first)`() {
        repository.storeTransaction(accountId, BigDecimal("10.00"), "First")
        repository.storeTransaction(accountId, BigDecimal("20.00"), "Second")

        val response = repository.getTransactions(accountId, Pageable(1, 10, Sort.DESC))

        assertEquals(2, response.transactions.size)
        assertEquals("Second", response.transactions[0].description)
        assertEquals("First", response.transactions[1].description)
    }

    @Test
    fun `getTransactions should support ASC order (oldest first)`() {
        repository.storeTransaction(accountId, BigDecimal("10.00"), "First")
        repository.storeTransaction(accountId, BigDecimal("20.00"), "Second")

        val response = repository.getTransactions(accountId, Pageable(1, 10, Sort.ASC))

        assertEquals("First", response.transactions[0].description)
        assertEquals("Second", response.transactions[1].description)
    }

    @Test
    fun `pagination should correctly calculate totalPages and nextPage`() {
        // Store 25 transactions
        for (i in 1..25) {
            repository.storeTransaction(accountId, BigDecimal("1.00"), "Tx $i")
        }

        // Page 1 (size 10)
        val page1 = repository.getTransactions(accountId, Pageable(1, 10, Sort.DESC))
        assertEquals(10, page1.transactions.size)
        assertEquals(2, page1.nextPage)
        assertEquals(3, page1.totalPages)

        // Page 3 (size 10)
        val page3 = repository.getTransactions(accountId, Pageable(3, 10, Sort.DESC))
        assertEquals(5, page3.transactions.size)
        assertNull(page3.nextPage)
    }

    @Test
    fun `getTransactions should return empty list gracefully for out of bounds page`() {
        repository.storeTransaction(accountId, BigDecimal("10.00"), "Only one")

        val response = repository.getTransactions(accountId, Pageable(5, 10, Sort.DESC))

        assertTrue(response.transactions.isEmpty())
        assertNull(response.nextPage)
        assertEquals(1, response.totalPages)
    }
}