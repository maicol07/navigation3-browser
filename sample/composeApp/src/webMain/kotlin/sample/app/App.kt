package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.github.terrakok.navigation3.browser.bindBackStackToBrowserHistory
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentParameters

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App() }

data object Main

data class Present(val id: Int)

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(Main) }

    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(
            backStack = backStack,
            saveItem = { key ->
                when (key) {
                    is Main -> buildBrowserHistoryFragment("main")
                    is Present -> buildBrowserHistoryFragment("present", mapOf("id" to key.id.toString()))
                    else -> null
                }.toString()
            },
            restoreItem = { fragment ->
                when (getBrowserHistoryFragmentName(fragment)) {
                    "main" -> Main
                    "present" -> Present(
                        getBrowserHistoryFragmentParameters(fragment).getValue("id")?.toInt() ?: error("id is required")
                    )

                    else -> null
                }
            }
        )
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onBuyPresent = { backStack.add(Present(it)) }
                )
            }
            entry<Present> { key ->
                PresentScreen(
                    id = key.id,
                    onBack = { backStack.removeLast() },
                    onChangePresent = {
                        backStack.removeLast()
                        backStack.add(Present(it))
                    }
                )
            }
        }
    )
}

private const val PRESENTS_COUNT = 3

@Composable
fun MainScreen(
    onBuyPresent: (id: Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        BasicText(
            text = "Buy a present!",
            color = { Color.White },
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Red)
                .padding(60.dp)
        )
        LazyColumn {
            items(PRESENTS_COUNT) { i ->
                BasicText(
                    text = "PRESENT $i",
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.LightGray)
                        .clickable(onClick = { onBuyPresent(i) })
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun PresentScreen(
    id: Int,
    onBack: () -> Unit,
    onChangePresent: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        BasicText(
            text = "Thank you! Your gift is on the way!",
            style = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
        )
        BasicText(
            text = "PRESENT $id",
            style = TextStyle.Default.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Blue)
                .padding(60.dp)
        )
        Row {
            BasicText(
                "DONE",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.LightGray)
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            )
            val otherPresent = (id + 1) % PRESENTS_COUNT
            BasicText(
                "Change to PRESENT `$otherPresent`",
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.LightGray)
                    .clickable(onClick = { onChangePresent(otherPresent) })
                    .padding(8.dp)
            )
        }
    }
}
