package dev.rumwerfer.couponvalidation_lib

import kotlinx.serialization.Serializable

enum class Store { Unknown, Aldi, Dm, Edeka, Lidl, Rewe }

@Serializable
data class CouponInput(val store: Store, val couponCode: String, val pin: String)