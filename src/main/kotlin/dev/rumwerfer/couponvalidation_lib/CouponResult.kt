package dev.rumwerfer.couponvalidation_lib

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

enum class CouponError {
    Unknown,
    InvalidFormat,
    InvalidCodeOrPin,
    Expired,
    UnknownCurrency,
}

data class Balance(val amount: BigDecimal, val currency: Currency) {
    companion object {
        operator fun invoke(amount: Double, currencyCode: String): Balance {
            val currency = Currency.getInstance(currencyCode)
            val amountAsDecimal = amount.toBigDecimal().setScale(currency.defaultFractionDigits, RoundingMode.HALF_UP)
            return Balance(amountAsDecimal, currency)
        }
    }
}

@Suppress("DataClassPrivateConstructor")
data class CouponResult private constructor(val balance: Balance?, val error: CouponError?) {
    constructor(balance: Balance) : this(balance, null)
    constructor(error: CouponError) : this(null, error)
}