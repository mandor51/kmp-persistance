import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mandor.kmp.persistance.KmpPersistence
import sample.app.App
import sample.app.CounterData
import java.awt.Dimension
import java.io.File

fun main() = application {
    Window(
        title = "sample",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        val scope = rememberCoroutineScope()
        App(
            dataStore = remember {
                KmpPersistence.createSecureDataStore(
                    path = { File(System.getProperty("user.home"), "counter.preferences_pb").absolutePath },
                    serializer = CounterData.serializer(),
                    defaultValue = CounterData(),
                    scope = scope
                )
            }
        )
    }
}