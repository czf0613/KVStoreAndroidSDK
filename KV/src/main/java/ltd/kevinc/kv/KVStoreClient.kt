package ltd.kevinc.kv

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import ltd.kevinc.kv.poco.DataModelDto
import ltd.kevinc.kv.poco.DataType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.Proxy
import java.util.concurrent.TimeUnit

object KVStoreClient {
    private lateinit var appId: String
    private lateinit var appKey: String
    private lateinit var userName: String
    private val jsonContentType = "application/json;charset=utf-8".toMediaType()
    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
    }
    private const val urlBase = "https://kv.kevinc.ltd"
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    fun initClient(appId: String, appKey: String, userName: String) {
        this.appId = appId
        this.appKey = appKey
        this.userName = userName
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun getObject(key: String): DataModelDto {
        val url = "$urlBase/data/manageData?key=$key&user=$userName"
        val request = Request.Builder()
            .url(url)
            .addHeader("X-AppId", appId)
            .addHeader("X-AppKey", appKey)
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val resp = httpClient.newCall(request).execute()
                jsonSerializer.decodeFromStream(resp.body!!.byteStream())
            } catch (e: Exception) {
                Log.e("ltd.kevinc.ltd", "KeyDoesNotExist: $key")

                DataModelDto(
                    isArray = false,
                    keyIdentifier = key,
                    userIdentifier = userName
                )
            }
        }
    }

    private suspend fun putObject(data: DataModelDto) {
        val url = "$urlBase/data/manageData"
        val requestBody = jsonSerializer.encodeToString(data).toRequestBody(jsonContentType)
        val request = Request.Builder()
            .url(url)
            .addHeader("X-AppId", appId)
            .addHeader("X-AppKey", appKey)
            .put(requestBody)
            .build()

        withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(request).execute()
            } catch (_: Exception) {
                Log.e("ltd.kevinc.ltd", "KeyUpdateFailed: ${data.keyIdentifier}")
            }
        }
    }

    suspend fun deleteKey(key: String) {
        val url = "$urlBase/data/manageData?key=$key&user=$userName"
        val request = Request.Builder()
            .url(url)
            .addHeader("X-AppId", appId)
            .addHeader("X-AppKey", appKey)
            .delete()
            .build()

        withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(request).execute()
            } catch (_: Exception) {
                Log.e("ltd.kevinc.ltd", "KeyDeleteFailed: $key")
            }
        }
    }

    suspend fun getInt(key: String): Int? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Int32) dto.int32Value else null
    }

    suspend fun setInt(key: String, value: Int) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            int32Value = value
        ).apply {
            setType(DataType.Int32)
        }

        putObject(dto)
    }
}