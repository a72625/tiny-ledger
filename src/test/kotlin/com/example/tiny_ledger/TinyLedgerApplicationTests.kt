package com.example.tiny_ledger

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationContext

@SpringBootTest
class TinyLedgerApplicationTests {

	@Test
	fun contextLoads(context: ApplicationContext) {
		assertThat(context).isNotNull()
	}

}
