import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.mandor.kmp.persistence.createDataStore
import platform.UIKit.UIViewController
import sample.app.App

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(dataStore = remember { createDataStore() })
}