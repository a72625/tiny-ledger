package com.example.tiny_ledger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class TinyLedgerApplication

fun main(args: Array<String>) {
    runApplication<TinyLedgerApplication>(*args)
}
