package com.example.tiny_ledger.domain.service

import com.example.tiny_ledger.domain.data.LedgerRepository
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.TransactionsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class LedgerService(
    private val repository: LedgerRepository,
    @Value($$"${ledger.max-precision}")
    private val maxPrecision: Int
) {
    fun openAccount(): AccountId {
        val id = AccountId(UUID.randomUUID())
        repository.openAccount(id)
        return id
    }

    fun getBalance(accountId: AccountId): BigDecimal {
        return repository.getBalance(accountId)
    }

    fun moneyMovement(accountId: AccountId, amount: BigDecimal, description: String?) {
        require(amount.signum() != 0) { "Amount cannot be zero" }
        require(amount.scale() <= maxPrecision) { "Precision too high" }

        repository.storeTransaction(accountId, amount, description)
    }

    fun getTransactions(accountId: AccountId, pageable: Pageable): TransactionsResponse {
        return repository.getTransactions(accountId, pageable)
    }

}