package com.github.terrakok.navigation3.browser

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
internal val BrowserHistoryIsInUse = AtomicBoolean(false)