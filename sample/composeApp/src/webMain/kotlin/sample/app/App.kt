package sample.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

data object RouteA

data class RouteB(val id: Int)

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Any>(RouteA) }

    LaunchedEffect(Unit) {
        bindBackStackToBrowserHistory(
            backStack = backStack,
            saveItem = { key ->
                when (key) {
                    is RouteA -> buildBrowserHistoryFragment("root")
                    is RouteB -> buildBrowserHistoryFragment("screenB", mapOf("id" to key.id.toString()))
                    else -> null
                }.toString()
            },
            restoreItem = { fragment ->
                when (getBrowserHistoryFragmentName(fragment)) {
                    "root" -> RouteA
                    "screenB" -> RouteB(
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
            entry<RouteA> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    BasicText("Route A")
                    LazyColumn {
                        items(10) { i ->
                            BasicText(
                                text = "Item $i",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.LightGray)
                                    .clickable(onClick = { backStack.add(RouteB(i)) })
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
            entry<RouteB> { key ->
                ScreenB(
                    id = key.id,
                    onBack = { backStack.removeLast() },
                    onReplace = {
                        backStack.removeLast()
                        backStack.add(RouteB(it))
                    }
                )
            }
        }
    )
}

@Composable
fun ScreenB(
    id: Int,
    onBack: () -> Unit,
    onReplace: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText("$id: Screen $id")
            Row {
                BasicText(
                    "Back",
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.LightGray)
                        .clickable(onClick = onBack)
                        .padding(8.dp)
                )
                BasicText(
                    "Replace with `${id + 1}`",
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.LightGray)
                        .clickable(onClick = { onReplace(id + 1) })
                        .padding(8.dp)
                )
            }
        }
    }
}