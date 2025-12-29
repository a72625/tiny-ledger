package com.example.tiny_ledger.domain.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration



@Configuration
@ConfigurationProperties(prefix = "currency")
class CurrencyConfig (
    var precision: Map<String, Int> = mutableMapOf()
)