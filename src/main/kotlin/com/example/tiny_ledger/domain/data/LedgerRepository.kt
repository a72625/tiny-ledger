package com.example.tiny_ledger.domain.data

import com.example.tiny_ledger.domain.exception.AccountNotFoundException
import com.example.tiny_ledger.domain.exception.InsufficientFundsException
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Currency
import com.example.tiny_ledger.domain.model.Ledger
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.Sort
import com.example.tiny_ledger.domain.model.Transaction
import com.example.tiny_ledger.domain.model.TransactionsResponse
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

@Repository
class LedgerRepository {
    private val data = ConcurrentHashMap<AccountId, Ledger>()

    fun openAccount(id: AccountId, currency: Currency) {
        data.computeIfAbsent(id) {
            Ledger(
                balance = BigDecimal.ZERO,
                transactions = ConcurrentLinkedDeque<Transaction>(),
                transactionsSize = 0,
                currency = currency
            )
        }
    }

    fun getAccountCurrency(accountId: AccountId): Currency = data[accountId]?.currency ?: throw AccountNotFoundException()

    fun getBalance(accountId: AccountId): BigDecimal {
        val ledger = data[accountId] ?: throw AccountNotFoundException()
        return ledger.balance
    }

    fun storeTransaction(accountId: AccountId, amount: BigDecimal, description: String?) {
        data.compute(accountId) { _, v ->
            if (v == null) {
                throw AccountNotFoundException()
            } else if (v.balance.add(amount) < BigDecimal.ZERO) {
                throw InsufficientFundsException()
            } else {
                val newBalance = v.balance.add(amount)
                val newSize = v.transactionsSize + 1
                v.transactions.addFirst(
                    Transaction(
                        dateTime = OffsetDateTime.now(),
                        description = description,
                        amount = amount,
                        balance = newBalance
                    )
                )
                v.copy(balance = newBalance, transactionsSize = newSize)
            }
        }
    }

    fun getTransactions(accountId: AccountId, pageable: Pageable): TransactionsResponse {
        val ledger = data[accountId] ?: throw AccountNotFoundException()

        // Capture the size at this specific moment in time
        val currentTotal = ledger.transactionsSize
        val start = (pageable.page - 1) * pageable.size
        val end = start + pageable.size

        val nextPage = if (currentTotal > end) pageable.page + 1 else null
        val totalPages = ((currentTotal + pageable.size - 1) / pageable.size).coerceAtLeast(1)

        // Handle out of bounds gracefully without an exception
        if (start >= currentTotal && currentTotal > 0) {
            return TransactionsResponse(
                page = pageable.page,
                nextPage = null,
                totalPages = totalPages,
                transactions = emptyList()
            )
        }

        val transactions = if (pageable.sort == Sort.ASC) {
            ledger.transactions.reversed().asSequence()
        } else {
            // Natural DESC
            ledger.transactions.asSequence()
        }


        val pagedList = transactions
            .drop(start)
            .take(pageable.size)
            .toList()

        return TransactionsResponse(
            page = pageable.page,
            nextPage = nextPage,
            totalPages = totalPages,
            transactions = pagedList
        )
    }

}