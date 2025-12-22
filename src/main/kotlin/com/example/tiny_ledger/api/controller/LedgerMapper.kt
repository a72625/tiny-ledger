package com.example.tiny_ledger.api.controller

import com.example.api.dto.PageSort
import com.example.api.dto.Transaction
import com.example.api.dto.TransactionsResponse
import com.example.tiny_ledger.domain.model.Sort
import com.example.tiny_ledger.domain.model.Transaction as DomainTransaction
import com.example.tiny_ledger.domain.model.TransactionsResponse as DomainTransactionResponse

class LedgerMapper {
    companion object {
        fun DomainTransactionResponse.toTransactionsResponse(): TransactionsResponse {
            return TransactionsResponse(
                transactions = this.transactions.map { it.toTransactionResponse() },
                page = page,
                totalPages = totalPages,
                nextPage = nextPage
            )

        }

        fun DomainTransaction.toTransactionResponse(): Transaction {
            return Transaction(
                dateTime = this.dateTime,
                amount = this.amount,
                balance = this.balance,
                description = this.description
            )
        }

        fun PageSort.toSort(): Sort {
            return when (this) {
                PageSort.ASC -> Sort.ASC
                PageSort.DESC -> Sort.DESC
            }
        }
    }
}