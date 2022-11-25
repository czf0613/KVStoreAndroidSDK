package ltd.kevinc.kv.poco

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class DataModelDto(
    val isArray: Boolean,
    val keyIdentifier: String,
    val userIdentifier: String,
    val int32Value: Int = 0,
    val int64Value: Long = 0L,
    val booleanValue: Boolean = false,
    val floatValue: Float = 0.0f,
    val doubleValue: Double = 0.0,
    val stringValue: String = "",
    val byteStringValue: String = "",
    val int32Values: List<Int> = emptyList(),
    val int64Values: List<Long> = emptyList(),
    val booleanValues: List<Boolean> = emptyList(),
    val floatValues: List<Float> = emptyList(),
    val doubleValues: List<Double> = emptyList(),
    val stringValues: List<String> = emptyList(),
) {
    var valueTypeIndicator: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    fun extractByteString(): ByteArray = Base64.getDecoder().decode(byteStringValue)

    fun getType(): DataType {
        return when (valueTypeIndicator) {
            0 -> DataType.Int32
            1 -> DataType.Int64
            // 2, 3不能用
            4 -> DataType.Boolean
            5 -> DataType.Float
            6 -> DataType.Double
            7 -> DataType.String
            8 -> DataType.Bytes
            else -> throw IllegalArgumentException("Unknown data type!")
        }
    }

    fun setType(type: DataType) {
        valueTypeIndicator = when (type) {
            DataType.Int32 -> 0
            DataType.Int64 -> 1
            DataType.Boolean -> 4
            DataType.Float -> 5
            DataType.Double -> 6
            DataType.String -> 7
            DataType.Bytes -> 8
            else -> throw IllegalArgumentException("Not supported data type!")
        }

        if (valueTypeIndicator == 8 && isArray)
            throw IllegalArgumentException("Not support multiple byte array!")
    }
}

enum class DataType {
    Int32,
    Int64,

    // 特别注意，Java平台里没有原生unsigned实现，所以这两个数据类型是不能用的
    UInt32,
    UInt64,
    Boolean,
    Float,
    Double,
    String,
    Bytes
}
