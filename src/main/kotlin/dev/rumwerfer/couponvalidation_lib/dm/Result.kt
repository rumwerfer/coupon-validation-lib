package dev.rumwerfer.couponvalidation_lib.dm

import kotlinx.serialization.Serializable

@Serializable
data class Result(val balance: Balance, val status: String)

@Serializable
data class Balance(val value: Double, val currency: String)
