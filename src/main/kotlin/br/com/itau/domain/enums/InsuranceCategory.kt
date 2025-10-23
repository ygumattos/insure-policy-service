package br.com.itau.domain.enums

enum class InsuranceCategory {
    VIDA,
    RESIDENCIAL,
    AUTO,
    OUTROS;

    companion object {
        fun from(value: String): InsuranceCategory =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: OUTROS
    }
}