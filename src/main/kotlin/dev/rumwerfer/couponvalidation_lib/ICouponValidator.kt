package dev.rumwerfer.couponvalidation_lib

interface ICouponValidator {
    suspend fun validate(couponInput: CouponInput) : CouponResult
}