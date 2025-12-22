package com.example.tiny_ledger.api.controller

import com.example.api.dto.ErrorResponse
import com.example.tiny_ledger.domain.exception.InsufficientFundsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("Account not found"))
    }
    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFunds(e: InsufficientFundsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse("Insufficient funds"))
    }
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleInsufficientFunds(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(e.message ?: "Bad request"))
    }
}