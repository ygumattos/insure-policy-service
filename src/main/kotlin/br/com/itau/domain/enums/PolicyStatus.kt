package br.com.itau.domain.enums

enum class PolicyStatus {
    RECEIVED,      // Solicitação criada
    VALIDATED,     // Após consulta à API de Fraudes
    PENDING,       // Aguardando pagamento e subscrição
    REJECTED,      // Rejeitado por regras ou pagamento
    APPROVED,      // Pagamento confirmado e subscrição aprovada
    CANCELLED      // Cancelado pelo cliente (exceto se aprovado)
}