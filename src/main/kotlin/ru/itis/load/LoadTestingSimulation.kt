package ru.itis.load

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.PopulationBuilder
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.readBytes
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

private const val PARALLEL_USERS = 1000

@Suppress("unused")
class LoadTestingSimulation : Simulation() {

    private val getDatabaseMetadataCase = RestApiCase(
        title = "Получение метаданных БД",
        requestsNumber = 20000,
        httpMethod = RestApiCase.HttpMethod.GET,
        uri = "https://google-gruyere.appspot.com/473727732335290923916520598210703254636/dump.gtl",
    )

    private val signInCase = RestApiCase(
        title = "Авторизация (REST API)",
        requestsNumber = 20000,
        httpMethod = RestApiCase.HttpMethod.GET,
        uri = "https://google-gruyere.appspot.com/519907068346070291793772695260874220617/login?uid=${UUID.randomUUID()}&pw=${UUID.randomUUID()}",
    )

    init {
        val cases = listOf(getDatabaseMetadataCase, signInCase)
        setUp(cases.buildScenarios())
    }

    private fun List<RestApiCase>.buildScenarios(): List<PopulationBuilder> =
        this.map { createScenarioBuilder(it).injectOpen(CoreDsl.atOnceUsers(PARALLEL_USERS)) }

    private fun createScenarioBuilder(case: RestApiCase): ScenarioBuilder =
        CoreDsl.scenario(case.title)
            .repeat(case.requestsNumber / PARALLEL_USERS)
            .on(
                buildScenario(case),
                CoreDsl.pace(1000.milliseconds.toJavaDuration())
            )

    private fun buildScenario(case: RestApiCase) = HttpDsl.http(case.title)
        .httpRequest(case.httpMethod.name, case.host + case.uri)
        .queryParamMap(case.queryParams)
        .headers(case.headers)
        .process(case.body) { actionBuilder, body -> actionBuilder.body(CoreDsl.StringBody(body)) }
        .process(case.bodyParts) { actionBuilder, bodyParts ->
            bodyParts
                .map { bodyPart ->
                    HttpDsl.ByteArrayBodyPart(bodyPart.name, bodyPart.getFileBytes())
                        .contentType(bodyPart.contentType)
                        .fileName(bodyPart.filename)
                        .process(bodyPart.charset) { bodyPartBuilder, charset ->
                            bodyPartBuilder.charset(charset)
                        }
                        .process(bodyPart.dispositionType) { bodyPartBuilder, dispositionType ->
                            bodyPartBuilder.dispositionType(dispositionType)
                        }
                        .process(bodyPart.transferEncoding) { bodyPartBuilder, transferEncoding ->
                            bodyPartBuilder.transferEncoding(transferEncoding)
                        }
                }
                .let { actionBuilder.bodyParts(it) }
        }
        .check(HttpDsl.status().lt(500))

    private fun <W, T> T.process(param: W?, func: (T, W) -> T): T =
        when (param) {
            is Boolean -> param
            is String -> param.isNotBlank()
            is Collection<*> -> param.isNotEmpty()
            else -> param != null
        }.let { if (it) func(this, param!!) else this }
}

data class RestApiCase(
    val title: String,
    var requestsNumber: Int,
    val body: String? = null,
    val httpMethod: HttpMethod,
    val host: String = "",
    val uri: String,
    val queryParams: Map<String, String> = emptyMap(),
    val headers: Map<String, String> =
        if (listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(httpMethod)) {
            mapOf("Content-Type" to "application/json")
        } else emptyMap(),
    val bodyParts: List<BodyPart> = emptyList(),
    val authRequired: Boolean = false,
) {
    @Suppress("unused")
    enum class HttpMethod {
        GET, PUT, POST, DELETE, PATCH, HEAD, OPTIONS, TRACE
    }

    data class BodyPart(
        val name: String,
        val contentType: String,
        val charset: String = "",
        val filename: String,
        val dispositionType: String = "",
        val transferEncoding: String = "",
        val filePath: String,
    ) {
        fun getFileBytes(): ByteArray = readFile(filePath)

        private fun readFile(filename: String): ByteArray =
            try {
                ClassLoader.getSystemResource(filename).toURI()
                    .let(Path::of)
                    .readBytes()
            } catch (ex: Exception) {
                throw ex
            }
    }
}