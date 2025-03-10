import dev.rumwerfer.couponvalidation_lib.CouponError
import dev.rumwerfer.couponvalidation_lib.CouponInput
import dev.rumwerfer.couponvalidation_lib.CouponValidator
import dev.rumwerfer.couponvalidation_lib.Store
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.math.BigDecimal
import kotlin.test.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CouponValidatorTests {
    companion object {
        private const val DUMMY_COUPON_CODE = "000100010001000100010001"
        private const val DUMMY_PIN = "1234"

        @JvmStatic
        private fun getValidCouponInputs(): Stream<CouponInput> {
            val validCodes = Json.decodeFromString<List<CouponInput>>(
                File("src/test/resources/valid_coupon_inputs.json").readText()
            )
            return validCodes.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("getValidCouponInputs")
    fun acceptValidCoupon(validCouponInput: CouponInput) = runTest {
        val result = CouponValidator().validate(validCouponInput)
        assertNull(result.error)
        assertNotEquals(BigDecimal.ZERO, result.balance!!.amount)
    }

    @ParameterizedTest
    @EnumSource(value = Store::class, names = ["Dm", "Unknown"])
    fun rejectInvalidCoupon(supportedStore: Store) = runTest {
        val input = CouponInput(supportedStore, DUMMY_COUPON_CODE, DUMMY_PIN)
        val result = CouponValidator().validate(input)
        assertEquals(CouponError.InvalidCodeOrPin, result.error)
        assertNull(result.balance)
    }

    @ParameterizedTest
    @EnumSource(value = Store::class, names = ["Aldi", "Edeka", "Lidl", "Rewe"])
    fun throwOnUnsupportedStore(unsupportedStore: Store) = runTest {
        val input = CouponInput(unsupportedStore, DUMMY_COUPON_CODE, DUMMY_PIN)
        val exception = assertThrows<IllegalArgumentException> { CouponValidator().validate(input) }
        assert(exception.message!!.contains(unsupportedStore.name))
    }
}