package br.com.itau.config


import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15"))
            .withDatabaseName("insurance_db")
            .withUsername("user")
            .withPassword("password")

        private val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            postgres.start()
            kafka.start()
        }

        @JvmStatic
        @AfterAll
        fun stopContainers() {
            kafka.stop()
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            // Datasource
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }

            // Kafka
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }

            // Endpoint Fraud (WireMock) — definiremos porta dinamicamente no teste
            registry.add("fraud.base-url") { "http://wiremock:8089" }
        }
    }
}