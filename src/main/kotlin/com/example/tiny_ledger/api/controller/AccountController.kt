package com.example.tiny_ledger.api.controller

import com.example.api.dto.AccountResponse
import com.example.api.dto.AccountOpeningRequest
import com.example.api.dto.BalanceResponse
import com.example.api.dto.MoneyMovement
import com.example.api.dto.PageSort
import com.example.api.dto.TransactionsResponse
import com.example.tiny_ledger.api.controller.LedgerMapper.Companion.toDomainCurrency
import com.example.tiny_ledger.api.controller.LedgerMapper.Companion.toSort
import com.example.tiny_ledger.api.controller.LedgerMapper.Companion.toTransactionsResponse
import com.example.tiny_ledger.domain.model.AccountId
import com.example.tiny_ledger.domain.model.Pageable
import com.example.tiny_ledger.domain.service.LedgerService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@RestController
@RequestMapping("/v1/accounts")
class AccountController(private val service: LedgerService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun openAccount(@Valid @RequestBody request: AccountOpeningRequest): AccountResponse {
        return AccountResponse(service.openAccount(request.currency.toDomainCurrency()).id)
    }

    @GetMapping("/{accountId}/balances")
    @ResponseStatus(HttpStatus.OK)
    fun getBalance(@PathVariable accountId: UUID): BalanceResponse {
        return BalanceResponse(
            balance = service.getBalance(AccountId(accountId))
        )
    }

    @PostMapping("/{accountId}/transactions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun moneyMovement(@PathVariable accountId: UUID, @Valid @RequestBody moneyMovement: MoneyMovement) {
        service.moneyMovement(AccountId(accountId), moneyMovement.amount, moneyMovement.description)
    }

    @GetMapping("/{accountId}/transactions")
    @ResponseStatus(HttpStatus.OK)
    fun getTransactions(
        @PathVariable accountId: UUID,
        @RequestParam(defaultValue = "1")
        @Min(value = 1, message = "Page must be at least 1")
        page: Int,
        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 100, message = "Size cannot exceed 100")
        size: Int,
        @RequestParam(defaultValue = "DESC")
        sort: PageSort
    ): TransactionsResponse {
        val pageable = Pageable(
            page = page, size = size, sort = sort.toSort()
        )

        return service.getTransactions(AccountId(accountId), pageable).toTransactionsResponse()
    }

}