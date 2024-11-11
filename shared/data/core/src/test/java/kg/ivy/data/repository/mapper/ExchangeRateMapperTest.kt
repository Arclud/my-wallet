package kg.ivy.data.repository.mapper

import kg.ivy.data.db.entity.ExchangeRateEntity
import kg.ivy.data.model.ExchangeRate
import kg.ivy.data.model.primitive.AssetCode
import kg.ivy.data.model.primitive.PositiveDouble
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test

class ExchangeRateMapperTest {

    private lateinit var mapper: ExchangeRateMapper

    @Before
    fun setup() {
        mapper = ExchangeRateMapper()
    }

    @Test
    fun `maps domain to entity`() {
        // given
        val mapper = ExchangeRateMapper()
        val exchangeRate =
            kg.ivy.data.model.ExchangeRate(
                baseCurrency = AssetCode.unsafe("USD"),
                currency = AssetCode.unsafe("AAVE"),
                rate = PositiveDouble.unsafe(0.000943049049897979),
                manualOverride = false,
            )

        // when
        val result = with(mapper) { exchangeRate.toEntity() }

        // then
        result shouldBe
                ExchangeRateEntity(
                    baseCurrency = "USD",
                    currency = "AAVE",
                    rate = 0.000943049049897979,
                    manualOverride = false,
                )
    }

    @Test
    fun `maps entity to domain`() {
        // given
        val exchangeRateEntity =
            ExchangeRateEntity(
                baseCurrency = "USD",
                currency = "AAVE",
                rate = 0.000943049049897979,
                manualOverride = false,
            )

        // when
        val result = with(mapper) { exchangeRateEntity.toDomain() }

        // then
        result.getOrNull() shouldBe kg.ivy.data.model.ExchangeRate(
            baseCurrency = AssetCode.unsafe("USD"),
            currency = AssetCode.unsafe("AAVE"),
            rate = PositiveDouble.unsafe(0.000943049049897979),
            manualOverride = false,
        )
    }
}
