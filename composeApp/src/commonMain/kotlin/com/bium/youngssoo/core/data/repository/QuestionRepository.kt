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

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val httpClient: HttpClient? = null,
    private val settings: Settings
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Firebase 프로젝트 ID (google-services.json에서 확인)
    private val projectId = "heyyoungssoo"

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
            var remoteVersion = 0
            
            println("[FirebaseVersionCheck] ========= 버전 체크 시작 =========")
            println("[FirebaseVersionCheck] Request: GET $versionUrl")
            
            try {
                val versionResponse: String = httpClient.get(versionUrl).body()
                println("[FirebaseVersionCheck] Response Raw Data: $versionResponse")
                
                val versionJson = json.parseToJsonElement(versionResponse).jsonObject
                val fields = versionJson["fields"]?.jsonObject
                remoteVersion = fields?.get(versionKey)?.jsonObject?.get("integerValue")?.jsonPrimitive?.intOrNull ?: 0
                
                println("[FirebaseVersionCheck] Parsed Remote Version ($versionKey): $remoteVersion")
            } catch (e: Exception) {
                println("[FirebaseVersionCheck] Error fetching version: ${e.message}")
                // 메타데이터가 없는 경우 등 에러 무시
            }

            val localVersion = settings.getInt(versionKey, 0)
            val hasLocalData = questionDao.getQuestionCount(category) > 0
            
            println("[FirebaseVersionCheck] Comparison - Local: $localVersion, Remote: $remoteVersion, HasLocalData: $hasLocalData")
            println("[FirebaseVersionCheck] ===================================")

            // forceRefresh가 아니고 로컬 데이터가 존재하며, 버전이 최신인 경우에만 스킵
            // remoteVersion이 0인 경우는 metadata 미설정 → 항상 싱크 (버전 관리 전 초기 상태)
            if (!forceRefresh && hasLocalData && remoteVersion > 0 && remoteVersion <= localVersion) {
                return Result.success(0)
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
                questionDao.insertQuestions(questions)
                if (remoteVersion > 0) {
                    settings.putInt(versionKey, remoteVersion)
                }
            }

            Result.success(questions.size)
        } catch (e: Exception) {
            // 네트워크 오류 시 로컬 데이터 사용
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
