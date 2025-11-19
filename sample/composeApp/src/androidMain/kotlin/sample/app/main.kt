package sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.mandor.kmp.persistance.KmpPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App(
                dataStore = remember {
                    KmpPersistence.createSecureDataStore(
                        path = { filesDir.resolve("counter.preferences_pb").absolutePath },
                        serializer = CounterData.serializer(),
                        defaultValue = CounterData(),
                        scope = lifecycleScope
                    )
                }
            )
        }
    }
}