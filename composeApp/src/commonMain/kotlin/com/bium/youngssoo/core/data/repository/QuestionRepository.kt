package com.bium.youngssoo.core.data.repository

import com.bium.youngssoo.core.data.local.QuestionCategory
import com.bium.youngssoo.core.data.local.QuestionDao
import com.bium.youngssoo.core.data.local.QuestionEntity
import com.bium.youngssoo.core.data.remote.QuestionDto
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val httpClient: HttpClient? = null,
    private val settings: Settings
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Firebase 프로젝트 ID (google-services.json에서 확인)
    private val projectId = "heyyoungssoo"

    // 버전 메타데이터 없을 때 재다운로드 최소 간격: 24시간
    private val cacheTtlMillis = 24 * 60 * 60 * 1000L

    // Settings에 Long 버전 저장 시 사용하는 키 suffix
    private fun versionLongKey(base: String) = "${base}_long"

    /**
     * Firebase에서 문제 데이터 동기화 (REST API 사용)
     * @param category 카테고리 (VOCAB, HANJA)
     * @param forceRefresh true면 무조건 서버에서 가져옴
     */
    suspend fun syncQuestions(
        category: QuestionCategory,
        forceRefresh: Boolean = false
    ): Result<Int> {
        return try {
            // HttpClient가 없거나 Firebase 연동이 안 된 경우 스킵
            if (httpClient == null) {
                return Result.success(0)
            }

            val versionKey = when (category) {
                QuestionCategory.VOCAB -> "vocab_version"
                QuestionCategory.HANJA -> "hanja_version"
            }

            // 버전 획득
            val versionUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/metadata/version"
            var remoteVersionLong = 0L

            println("[FirebaseVersionCheck] ========= 버전 체크 시작 =========")
            println("[FirebaseVersionCheck] Request: GET $versionUrl")
            try {
                val versionResponse: String = httpClient.get(versionUrl).body()
                println("[FirebaseVersionCheck] Response Raw Data: $versionResponse")

                val versionJson = json.parseToJsonElement(versionResponse).jsonObject
                val fields = versionJson["fields"]?.jsonObject
                // integerValue는 Long 범위 타임스탬프일 수 있으므로 longOrNull로 읽음
                remoteVersionLong = fields?.get(versionKey)?.jsonObject?.get("integerValue")
                    ?.jsonPrimitive?.longOrNull ?: 0L

                println("[FirebaseVersionCheck] Parsed Remote Version ($versionKey): $remoteVersionLong")
            } catch (e: Exception) {
                println("[FirebaseVersionCheck] Error fetching version: ${e.message}")
            }

            val localVersionLong = settings.getLong(versionLongKey(versionKey), 0L)
            val lastSyncKey = "${versionKey}_last_sync"
            val lastSyncTime = settings.getLong(lastSyncKey, 0L)
            val hasLocalData = questionDao.getQuestionCount(category) > 0
            val now = Clock.System.now().toEpochMilliseconds()

            println("[FirebaseVersionCheck] Comparison - Local: $localVersionLong, Remote: $remoteVersionLong, HasLocalData: $hasLocalData, LastSync: $lastSyncTime")
            println("[FirebaseVersionCheck] ===================================")

            if (!forceRefresh && hasLocalData) {
                // 버전 정보가 있는 경우: 버전 비교 (Long 기반)
                if (remoteVersionLong > 0L && remoteVersionLong <= localVersionLong) {
                    return Result.success(0)
                }
                // 버전 정보가 없는 경우(remoteVersion == 0): TTL 기반 캐시 (24시간)
                if (remoteVersionLong == 0L && (now - lastSyncTime) < cacheTtlMillis) {
                    return Result.success(0)
                }
            }

            val collectionName = when (category) {
                QuestionCategory.VOCAB -> "vocab_questions"
                QuestionCategory.HANJA -> "hanja_questions"
            }

            // Firestore REST API 호출 (페이지네이션으로 전체 데이터 수집)
            val baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/$collectionName"
            val allDocuments = mutableListOf<kotlinx.serialization.json.JsonElement>()
            var pageToken: String? = null

            do {
                val url = if (pageToken != null) "$baseUrl?pageToken=$pageToken" else baseUrl
                val response: String = httpClient.get(url).body()
                val jsonResponse = json.parseToJsonElement(response).jsonObject
                jsonResponse["documents"]?.jsonArray?.let { allDocuments.addAll(it) }
                pageToken = jsonResponse["nextPageToken"]?.jsonPrimitive?.content
            } while (pageToken != null)

            if (allDocuments.isEmpty()) return Result.success(0)

            val questions = allDocuments.mapNotNull { docElement ->
                try {
                    val doc = docElement.jsonObject
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                    val name = doc["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val id = name.substringAfterLast("/")

                    val question = fields["question"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val answer = fields["answer"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val grade = fields["grade"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val difficulty = fields["difficulty"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.intOrNull ?: 1
                    val updatedAt = fields["updatedAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.longOrNull ?: 0L

                    val optionsArray = fields["options"]?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
                    val options = optionsArray?.mapNotNull {
                        it.jsonObject["stringValue"]?.jsonPrimitive?.content
                    } ?: emptyList()

                    QuestionEntity(
                        id = id,
                        category = category,
                        question = question,
                        answer = answer,
                        options = json.encodeToString(
                            ListSerializer(String.serializer()),
                            options
                        ),
                        grade = grade,
                        difficulty = difficulty,
                        updatedAt = updatedAt
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (questions.isNotEmpty()) {
                // 버전이 업데이트됐거나 강제 갱신 시 → 기존 데이터 전체 삭제 후 재삽입
                val isVersionChanged = remoteVersionLong > 0L && remoteVersionLong != localVersionLong
                if (forceRefresh || isVersionChanged) {
                    questionDao.deleteQuestionsByCategory(category)
                }
                questionDao.insertQuestions(questions)
                if (remoteVersionLong > 0L) {
                    settings.putLong(versionLongKey(versionKey), remoteVersionLong)
                }
                // 버전 유무와 관계없이 마지막 동기화 시간 항상 저장
                settings.putLong(lastSyncKey, now)
            }

            Result.success(questions.size)
        } catch (e: Exception) {
            // 네트워크 오류 시 로컬 데이터 사용
            Result.failure(e)
        }
    }

    /**
     * 특정 급수(Grade)만 선택하여 Firestore에서 데이터 동기화
     */
    suspend fun syncQuestionsByGrades(
        category: QuestionCategory,
        grades: Set<String>
    ): Result<Int> {
        return try {
            if (httpClient == null || grades.isEmpty()) {
                return Result.success(0)
            }

            val collectionName = when (category) {
                QuestionCategory.VOCAB -> "vocab_questions"
                QuestionCategory.HANJA -> "hanja_questions"
            }

            // IN 쿼리는 최대 10개까지 지원하므로, 10개씩 분할하여 요청
            val allDocuments = mutableListOf<kotlinx.serialization.json.JsonElement>()
            val chunkedGrades = grades.chunked(10)

            val queryUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents:runQuery"

            for (chunk in chunkedGrades) {
                val requestBody = buildJsonObject {
                    putJsonObject("structuredQuery") {
                        putJsonArray("from") {
                            add(buildJsonObject { put("collectionId", collectionName) })
                        }
                        putJsonObject("where") {
                            putJsonObject("fieldFilter") {
                                putJsonObject("field") {
                                    put("fieldPath", "grade")
                                }
                                put("op", "IN")
                                putJsonObject("value") {
                                    putJsonObject("arrayValue") {
                                        putJsonArray("values") {
                                            chunk.forEach { grade ->
                                                add(buildJsonObject { put("stringValue", grade) })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val response: String = httpClient.post(queryUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody.toString())
                }.body()

                val jsonElements = json.parseToJsonElement(response).jsonArray
                allDocuments.addAll(jsonElements)
            }

            if (allDocuments.isEmpty()) return Result.success(0)

            val questions = allDocuments.mapNotNull { docElement ->
                try {
                    val docObj = docElement.jsonObject
                    // runQuery 결과는 배열 안에 {"document": {...}, "readTime": "..."} 형태임
                    val doc = docObj["document"]?.jsonObject ?: return@mapNotNull null
                    val fields = doc["fields"]?.jsonObject ?: return@mapNotNull null
                    val name = doc["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val id = name.substringAfterLast("/")

                    val question = fields["question"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val answer = fields["answer"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val grade = fields["grade"]?.jsonObject?.get("stringValue")?.jsonPrimitive?.content ?: ""
                    val difficulty = fields["difficulty"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.intOrNull ?: 1
                    val updatedAt = fields["updatedAt"]?.jsonObject?.get("integerValue")?.jsonPrimitive?.longOrNull ?: 0L

                    val optionsArray = fields["options"]?.jsonObject?.get("arrayValue")?.jsonObject?.get("values")?.jsonArray
                    val options = optionsArray?.mapNotNull {
                        it.jsonObject["stringValue"]?.jsonPrimitive?.content
                    } ?: emptyList()

                    QuestionEntity(
                        id = id,
                        category = category,
                        question = question,
                        answer = answer,
                        options = json.encodeToString(
                            ListSerializer(String.serializer()),
                            options
                        ),
                        grade = grade,
                        difficulty = difficulty,
                        updatedAt = updatedAt
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (questions.isNotEmpty()) {
                questionDao.insertQuestions(questions)
            }

            Result.success(questions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로컬 DB에서 문제 가져오기
     */
    suspend fun getQuestions(category: QuestionCategory): List<QuestionEntity> {
        return questionDao.getQuestionsByCategory(category)
    }

    /**
     * 난이도별 문제 가져오기
     */
    suspend fun getQuestionsByDifficulty(
        category: QuestionCategory,
        difficulty: Int
    ): List<QuestionEntity> {
        return questionDao.getQuestionsByCategoryAndDifficulty(category, difficulty)
    }

    /**
     * 랜덤 문제 가져오기
     */
    suspend fun getRandomQuestions(
        category: QuestionCategory,
        count: Int
    ): List<QuestionEntity> {
        val allQuestions = questionDao.getQuestionsByCategory(category)
        return allQuestions.shuffled().take(count)
    }

    /**
     * 문제 개수 확인
     */
    suspend fun getQuestionCount(category: QuestionCategory): Int {
        return questionDao.getQuestionCount(category)
    }

    /**
     * 선택지 파싱
     */
    fun parseOptions(optionsJson: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(optionsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
