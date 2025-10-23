package br.com.itau.domain.entities

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConfigurationProperties(prefix = "policy.evaluation")
data class PolicyEvaluationConfig(
    var regular: LimitConfig = LimitConfig(),
    var highRisk: LimitConfig = LimitConfig(),
    var preferential: LimitConfig = LimitConfig(),
    var noInfo: LimitConfig = LimitConfig()
) {
    data class LimitConfig(
        var vidaResidencial: BigDecimal? = null,
        var auto: BigDecimal? = null,
        var outros: BigDecimal? = null,
        var vida: BigDecimal? = null,
        var autoResidencial: BigDecimal? = null,
        var residencial: BigDecimal? = null
    )
}