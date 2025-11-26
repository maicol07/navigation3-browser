package com.github.terrakok.navigation3.browser

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Composable
fun ConfigureBrowserBack(
    backStack: SnapshotStateList<Any>
) {
    val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
        ?: error("NavigationEventDispatcher not found.")
    val input = remember { DirectNavigationEventInput() }
    DisposableEffect(navigationEventDispatcher) {
        navigationEventDispatcher.addInput(input)
        onDispose { navigationEventDispatcher.removeInput(input) }
    }

    LaunchedEffect(Unit) {
        configureBrowserBack(
            backStack = backStack,
            onBack = { input.backCompleted() }
        )
    }
}

private const val CURRENT_ENTRY = "compose_current_entry"
private const val ROOT_ENTRY = "compose_root_entry"

@OptIn(ExperimentalAtomicApi::class)
private suspend fun configureBrowserBack(
    backStack: SnapshotStateList<Any>,
    onBack: () -> Unit,
) {
    val firstBind = BrowserHistoryIsInUse.compareAndSet(expectedValue = false, newValue = true)
    if (!firstBind) {
        val window = refBrowserWindow()
        window.console.warn("BrowserHistory is already bound to another backstack")
        return
    }
    try {
        coroutineScope {
            val window = refBrowserWindow()
            val appAddress = with(window.location) { origin + pathname }
            val rootDestination = backStack.lastOrNull()?.toString().orEmpty()
            window.history.replaceState(ROOT_ENTRY, "", "$appAddress#$rootDestination")

            //listen browser navigation events
            launch {
                window.popStateEvents()
                    .map { it.state }
                    .collect { state ->
                        if (state == ROOT_ENTRY) {
                            onBack()
                        }
                    }
            }

            //listen backstack's changes
            launch {
                snapshotFlow { backStack.toList() }
                    .drop(1) //initial value
                    .collect { stack ->
                        if (stack.isEmpty()) return@collect
                        val name = stack.last().toString()

                        if (window.history.state == ROOT_ENTRY) {
                            window.history.pushState(CURRENT_ENTRY, "", "$appAddress#$name")
                        } else {
                            window.history.replaceState(CURRENT_ENTRY, "", "$appAddress#$name")
                        }
                    }
            }
        }
    } catch (_: CancellationException) {
        BrowserHistoryIsInUse.store(false)
    }
}
