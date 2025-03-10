package dev.rumwerfer.couponvalidation_lib

class CouponValidator : ICouponValidator {
    override suspend fun validate(couponInput: CouponInput) : CouponResult {
        if (couponInput.store !in arrayOf(Store.Dm, Store.Unknown)) {
            throw IllegalArgumentException("${couponInput.store.name} wird derzeit nicht unterstützt")
        }

        val dmCouponValidator = DmCouponValidator()
        return dmCouponValidator.validate(couponInput)
    }
}