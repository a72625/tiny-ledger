package com.example.tiny_ledger.domain.service

import com.example.tiny_ledger.api.controller.AccountController.Companion.MAXIMUM_PRECISION
import com.example.tiny_ledger.domain.data.LedgerRepository
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.TransactionsResponse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class LedgerService(
    private val repository: LedgerRepository
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
        require(amount.scale() <= MAXIMUM_PRECISION) { "Precision too high" }

        repository.storeTransaction(accountId, amount, description)
    }

    fun getTransactions(accountId: AccountId, pageable: Pageable): TransactionsResponse {
        return repository.getTransactions(accountId, pageable)
    }

}