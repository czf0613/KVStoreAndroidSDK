package ltd.kevinc.kcloudkit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ltd.kevinc.kv.KVStoreClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        KVStoreClient.initClient(
            "456e2d0e3ab14e9c8de566b5f52b6a79",
            "29bfece091f94356aa8cff679e852294",
            "13336472640"
        )

        lifecycleScope.launch {
//            KVStoreClient.setInt("happy_test", 114514)
//            delay(1000)
//            val happy = KVStoreClient.getInt("happy_test")
//            Log.i("result", "saved int is: $happy")
//            KVStoreClient.deleteKey("happy_test")

            KVStoreClient.setIntArray("int_arr", listOf(1, 2, 3, 4, 5))
            delay(1000)
            val arr = KVStoreClient.getIntArray("int_arr") ?: emptyList()
            Log.i("result", "saved int_arr is: ${arr.joinToString(", ")}")
        }
    }
}