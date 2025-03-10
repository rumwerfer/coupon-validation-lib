package dev.rumwerfer.couponvalidation_lib

import dev.rumwerfer.couponvalidation_lib.dm.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DmCouponValidator : ICouponValidator {
    private val logger: Logger = LoggerFactory.getLogger(DmCouponValidator::class.java)

    private val validationUrl = "https://dmpay-gateway.services.dmtech.com/checker/DE/credits"
    private val couponCodeHeader = "X-Printed-Credit-Key"
    private val pinHeader = "X-Verification-Code"

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json (Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun validate(couponInput: CouponInput) : CouponResult {
        if (!validateFormat(couponInput)) {
            return CouponResult(CouponError.InvalidFormat)
        }

        val response = httpClient.get(validationUrl) {
            headers {
                append(couponCodeHeader, couponInput.couponCode)
                append(pinHeader, couponInput.pin)
            }
        }

        val dmResult: Result

        try {
            dmResult = response.body<Result>()
        } catch (e: Exception) {
            if (response.status == HttpStatusCode.BadRequest) {
                return CouponResult(CouponError.InvalidCodeOrPin)
            } else {
                logger.debug("${response.status.value} ${e.message}")
                return CouponResult(CouponError.Unknown)
            }
        }

        return getResult(dmResult)
    }

    private fun getResult(dmResult: Result): CouponResult {
        if (dmResult.status != "ACTIVE") {
            logger.debug(dmResult.status)
            return CouponResult(CouponError.Expired)
        }

        try {
            val balance = Balance(dmResult.balance.value, dmResult.balance.currency)
            return CouponResult(balance)
        } catch (e: IllegalArgumentException) {
            logger.debug(dmResult.balance.currency)
            return CouponResult(CouponError.UnknownCurrency)
        }
    }

    private fun validateFormat(couponInput: CouponInput): Boolean =
        validateCouponCode(couponInput.couponCode) && validatePin(couponInput.pin)

    private fun validateCouponCode(couponCode: String) = couponCode.length == 24 && couponCode.all { x -> x.isDigit() }

    private fun validatePin(pin: String) = pin.length == 4 && pin.all { x -> x.isDigit() }
}