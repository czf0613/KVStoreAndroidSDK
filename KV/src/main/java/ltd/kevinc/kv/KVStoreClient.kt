package ltd.kevinc.kv

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

    suspend fun getLong(key: String): Long? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Int64) dto.int64Value else null
    }

    suspend fun setLong(key: String, value: Long) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            int64Value = value
        ).apply {
            setType(DataType.Int64)
        }

        putObject(dto)
    }

    suspend fun getBoolean(key: String): Boolean? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Boolean) dto.booleanValue else null
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            booleanValue = value
        ).apply {
            setType(DataType.Boolean)
        }

        putObject(dto)
    }

    suspend fun getFloat(key: String): Float? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Float) dto.floatValue else null
    }

    suspend fun setFloat(key: String, value: Float) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            floatValue = value
        ).apply {
            setType(DataType.Float)
        }

        putObject(dto)
    }

    suspend fun getDouble(key: String): Double? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Double) dto.doubleValue else null
    }

    suspend fun setDouble(key: String, value: Double) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            doubleValue = value
        ).apply {
            setType(DataType.Double)
        }

        putObject(dto)
    }

    suspend fun getString(key: String): String? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Double) dto.stringValue else null
    }

    suspend fun setString(key: String, value: String) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName,
            stringValue = value
        ).apply {
            setType(DataType.String)
        }

        putObject(dto)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getByteArray(key: String): ByteArray? {
        val dto = getObject(key)

        return if (dto.getType() == DataType.Bytes) dto.extractByteString() else null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun setByteArray(key: String, value: ByteArray) {
        val dto = DataModelDto(
            isArray = false,
            keyIdentifier = key,
            userIdentifier = userName
        ).apply {
            encodeByteString(value)
        }

        putObject(dto)
    }

    suspend fun getIntArray(key: String): List<Int>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.Int32) dto.int32Values else null
    }

    suspend fun setIntArray(key: String, value: List<Int>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            int32Values = value
        ).apply {
            setType(DataType.Int32)
        }

        putObject(dto)
    }

    suspend fun getLongArray(key: String): List<Long>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.Int64) dto.int64Values else null
    }

    suspend fun setLongArray(key: String, value: List<Long>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            int64Values = value
        ).apply {
            setType(DataType.Int64)
        }

        putObject(dto)
    }

    suspend fun getBooleanArray(key: String): List<Boolean>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.Boolean) dto.booleanValues else null
    }

    suspend fun setBooleanArray(key: String, value: List<Boolean>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            booleanValues = value
        ).apply {
            setType(DataType.Boolean)
        }

        putObject(dto)
    }

    suspend fun getFloatArray(key: String): List<Float>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.Float) dto.floatValues else null
    }

    suspend fun setFloatArray(key: String, value: List<Float>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            floatValues = value
        ).apply {
            setType(DataType.Float)
        }

        putObject(dto)
    }

    suspend fun getDoubleArray(key: String): List<Double>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.Double) dto.doubleValues else null
    }

    suspend fun setDoubleArray(key: String, value: List<Double>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            doubleValues = value
        ).apply {
            setType(DataType.Double)
        }

        putObject(dto)
    }

    suspend fun getStringArray(key: String): List<String>? {
        val dto = getObject(key)

        return if (dto.isArray && dto.getType() == DataType.String) dto.stringValues else null
    }

    suspend fun setStringArray(key: String, value: List<String>) {
        val dto = DataModelDto(
            isArray = true,
            keyIdentifier = key,
            userIdentifier = userName,
            stringValues = value
        ).apply {
            setType(DataType.String)
        }

        putObject(dto)
    }
}