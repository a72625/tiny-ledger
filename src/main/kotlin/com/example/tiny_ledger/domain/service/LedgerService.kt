package com.example.tiny_ledger.domain.service

import com.example.tiny_ledger.domain.data.LedgerRepository
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Currency
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.model.TransactionsResponse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class LedgerService(
    private val repository: LedgerRepository,
    private val currencyConfig: CurrencyConfig
) {
    fun openAccount(currency: Currency): AccountId {
        val id = AccountId(UUID.randomUUID())
        repository.openAccount(id, currency)
        return id
    }

    fun getBalance(accountId: AccountId): BigDecimal {
        return repository.getBalance(accountId)
    }

    fun moneyMovement(accountId: AccountId, amount: BigDecimal, description: String?) {
        require(amount.signum() != 0) { "Amount cannot be zero" }
        val currency = repository.getAccountCurrency(accountId)
        val precision = currencyConfig.precision[currency.name] ?: throw IllegalStateException()
        require(amount.scale() <= precision) { "Precision too high" }

        repository.storeTransaction(accountId, amount, description)
    }

    fun getTransactions(accountId: AccountId, pageable: Pageable): TransactionsResponse {
        return repository.getTransactions(accountId, pageable)
    }

}