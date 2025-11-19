import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import com.mandor.kmp.persistance.KmpPersistence
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.builtins.serializer
import platform.UIKit.UIViewController
import sample.app.App
import sample.app.CounterData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController {
    val scope = rememberCoroutineScope()
    App(
        dataStore = remember {
            KmpPersistence.createSecureDataStore(
                path = {
                    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null
                    )
                    requireNotNull(documentDirectory).path + "/counter.preferences_pb"
                },
                serializer = CounterData.serializer(),
                defaultValue = CounterData(),
                scope = scope
            )
        }
    )
}