package br.com.itau.adapters.controllers

import br.com.itau.adapters.controllers.dtos.CreatePolicyRequestDto
import br.com.itau.adapters.controllers.dtos.CreatePolicyResponseDto
import br.com.itau.adapters.controllers.dtos.PolicyResponseDto
import br.com.itau.adapters.controllers.mappers.createPolicyMappers.toCreateDTO
import br.com.itau.adapters.controllers.mappers.createPolicyMappers.toDTO
import br.com.itau.adapters.controllers.mappers.createPolicyMappers.toDomain
import br.com.itau.application.ports.inputs.ManagePolicyLifecycleUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/policies")
class PolicyRequestController(
    private val managePolicyLifecycleUseCase: ManagePolicyLifecycleUseCase
) {

    @PostMapping("/create")
    fun createPolicyRequest(
        @Valid @RequestBody request: CreatePolicyRequestDto
    ): ResponseEntity<CreatePolicyResponseDto> {
        val policyRequest = managePolicyLifecycleUseCase.create(request.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).body(policyRequest.toCreateDTO())
    }

    @GetMapping("/{policyRequestId}")
    fun getPolicyRequest(
        @PathVariable policyRequestId: String
    ): ResponseEntity<PolicyResponseDto> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(managePolicyLifecycleUseCase.getById(policyRequestId).toDTO())
    }

    @DeleteMapping("/{policyRequestId}")
    fun cancelPolicyRequest(
        @PathVariable policyRequestId: String
    ): ResponseEntity<Void> {
        managePolicyLifecycleUseCase.cancel(policyRequestId)
        return ResponseEntity.noContent().build()
    }
}