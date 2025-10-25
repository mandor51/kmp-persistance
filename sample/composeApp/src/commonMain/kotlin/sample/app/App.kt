package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun App(
    dataStore: DataStore<Preferences>
) {

    val counter by dataStore
        .data
        .map {
            val counterKey = intPreferencesKey("counter")
            it[counterKey] ?: 0
        }
        .collectAsState(initial = 0)

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Hello, KMP! counter: $counter")
        Button(
            onClick = {
                scope.launch {
                    dataStore.edit { preferences ->
                        val counterKey = intPreferencesKey("counter")
                        preferences[counterKey] = counter + 1
                    }
                }
            }
        ) {
            Text("Click Me")
        }
    }
}