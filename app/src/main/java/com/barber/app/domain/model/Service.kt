package com.barber.app.domain.model

import java.math.BigDecimal

data class Service(
    val id: Long,
    val name: String,
    val description: String,
    val estimatedMinutes: Int,
    val price: BigDecimal,
)
