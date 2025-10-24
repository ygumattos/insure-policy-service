package br.com.itau.adapters.repositories.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "policy_requests")
class PolicyRequestEntity(
    @Id
    val id: String,

    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Column(name = "product_id", nullable = false)
    val productId: String,

    @Column(nullable = false, length = 50)
    val category: String,

    @Column(name = "sales_channel", nullable = false, length = 50)
    val salesChannel: String,

    @Column(name = "payment_method", nullable = false, length = 50)
    val paymentMethod: String,

    @Column(name = "total_monthly_premium_amount", nullable = false)
    val totalMonthlyPremiumAmount: BigDecimal,

    @Column(name = "insured_amount", nullable = false)
    val insuredAmount: BigDecimal,

    @Column(nullable = false, length = 20)
    val status: String,

    @Column()
    val classification: String? = null,

    @Column(name = "payment_confirmation")
    var paymentConfirmation: Boolean? = null,

    @Column(name = "subscription_authorization")
    var subscriptionAutorization: Boolean? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "finished_at")
    var finishedAt: Instant? = null,

    @OneToMany(mappedBy = "policyRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
    val coverages: MutableList<CoverageEntity> = mutableListOf(),

    @OneToMany(mappedBy = "policyRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
    val assistances: MutableList<AssistanceEntity> = mutableListOf(),

    @OneToMany(mappedBy = "policyRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("changedAt ASC")
    val statusHistory: MutableList<StatusHistoryEntity> = mutableListOf()
)

@Entity
@Table(name = "policy_coverages")
class CoverageEntity(
    @Id
    @Column(length = 36)
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_request_id")
    val policyRequest: PolicyRequestEntity,

    @Column(name = "coverage_name", nullable = false, length = 100)
    val coverageName: String,

    @Column(name = "coverage_value", nullable = false)
    val coverageValue: BigDecimal
)

@Entity
@Table(name = "policy_assistances")
class AssistanceEntity(
    @Id
    @Column(length = 36)
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_request_id")
    val policyRequest: PolicyRequestEntity,

    @Column(name = "assistance_name", nullable = false, length = 100)
    val assistanceName: String
)

@Entity
@Table(name = "policy_status_history")
class StatusHistoryEntity(
    @Id
    @Column(length = 36)
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_request_id")
    val policyRequest: PolicyRequestEntity,

    @Column(nullable = false, length = 20)
    val status: String,

    @Column(name = "changed_at", nullable = false)
    val changedAt: Instant
)