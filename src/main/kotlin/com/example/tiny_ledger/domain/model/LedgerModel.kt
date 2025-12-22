package com.example.tiny_ledger.domain.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedDeque

@JvmInline
value class AccountId(val id: UUID){
    override fun toString(): String = id.toString()
}

data class Transaction(
    val dateTime: OffsetDateTime,
    val description: String?,
    val amount: BigDecimal,
    val balance: BigDecimal
)

data class Ledger(
    val balance: BigDecimal,
    val transactions: ConcurrentLinkedDeque<Transaction>,
    val transactionsSize: Int
)

enum class Sort{
    ASC,
    DESC
}

data class Pageable(
    val page: Int,
    val size: Int,
    val sort: Sort
)

data class TransactionsResponse(
    val page: Int,
    val nextPage: Int?,
    val totalPages: Int,
    val transactions: List<Transaction>
)