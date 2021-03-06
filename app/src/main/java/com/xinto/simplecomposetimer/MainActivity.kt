/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xinto.simplecomposetimer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults.elevation
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xinto.simplecomposetimer.ui.theme.MyTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    @ObsoleteCoroutinesApi
    @ExperimentalUnsignedTypes
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

// Start building your app here!
@ObsoleteCoroutinesApi
@ExperimentalAnimationApi
@Composable
fun MyApp() {
    var time by remember { mutableStateOf("00:00:00") }
    val hour = remember { mutableStateOf("00") }
    val minute = remember { mutableStateOf("00") }
    val second = remember { mutableStateOf("00") }
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(TimerState.AWAITING_INPUT) }
    val backgroundColor = MaterialTheme.colors.background
    var coroutineJob: Job? = null
    var tickerChannel: ReceiveChannel<Unit>? = null
    val playIcon = Icons.Default.PlayArrow
    val stopIcon = Icons.Default.Stop

    Surface(color = backgroundColor) {
        Box(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                backgroundColor = backgroundColor,
                elevation = if (MaterialTheme.colors.isLight) 4.dp else 0.dp,
                contentPadding = PaddingValues(start = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    fontSize = 18.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp, 8.dp, 64.dp)
                    .align(Alignment.Center)
            ) {
                AnimatedVisibility(
                    visible = currentScreen == TimerState.AWAITING_INPUT,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    initiallyVisible = true
                ) {
                    Row {
                        TimeInputField(
                            value = hour,
                            maxValue = 24,
                            label = "Hours"
                        )
                        TimeInputField(
                            value = minute,
                            maxValue = 60,
                            label = "Minutes"
                        )
                        TimeInputField(
                            value = second,
                            maxValue = 60,
                            label = "Seconds"
                        )
                    }
                }
                AnimatedVisibility(
                    visible = currentScreen == TimerState.RUNNING,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Text(
                        text = time,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 32.sp
                    )
                }
            }
            FloatingActionButton(
                modifier = Modifier
                    .padding(PaddingValues(bottom = 32.dp))
                    .align(Alignment.BottomCenter),
                onClick = {

                    if (currentScreen == TimerState.RUNNING) {
                        currentScreen = TimerState.AWAITING_INPUT
                        tickerChannel?.cancel()
                        coroutineJob?.cancel()
                        return@FloatingActionButton
                    }

                    if (hour.value.isEmpty()) hour.value = "00"
                    if (minute.value.isEmpty()) minute.value = "00"
                    if (second.value.isEmpty()) second.value = "00"

                    val millisHour = TimeUnit.HOURS.toMillis(hour.value.toLong())
                    val millisMinute = TimeUnit.MINUTES.toMillis(minute.value.toLong())
                    val millisSecond = TimeUnit.SECONDS.toMillis(second.value.toLong())
                    var totalMillis = millisHour + millisMinute + millisSecond
                    coroutineJob = coroutineScope.launch {
                        tickerChannel = ticker(1000L, 0L, Dispatchers.IO)
                        for (event in tickerChannel!!) {
                            time = "${((totalMillis / 1000) / 3600).doubleHourUnit}:${((totalMillis / 1000) / 60).doubleHourUnit}:${((totalMillis / 1000) % 60).doubleHourUnit}"

                            if (totalMillis <= 0) {
                                currentScreen = TimerState.AWAITING_INPUT
                                tickerChannel?.cancel()
                                cancel()
                            }

                            totalMillis -= 1000
                        }
                    }
                    currentScreen = TimerState.RUNNING
                },
                elevation = elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Icon(
                    imageVector = if (currentScreen == TimerState.RUNNING) stopIcon else playIcon,
                    contentDescription = "Start Timer"
                )
            }
        }
    }
}

val Long.doubleHourUnit: String get() {
    val asString = toString()
    return if (asString.length < 2) "0$asString" else asString
}

@Composable
fun RowScope.TimeInputField(
    value: MutableState<String>,
    maxValue: Int,
    label: String
) {
    OutlinedTextField(
        value = value.value,
        onValueChange = { tfs ->
            var formattedValue = tfs.filter { it.isDigit() }
            if (formattedValue.isNotEmpty() && (formattedValue.toInt() > maxValue || formattedValue.length > 2)) {
                formattedValue = "$maxValue"
            }
            value.value = formattedValue
        },
        label = {
            Text(text = label)
        },
        modifier = Modifier
            .weight(1f)
            .padding(8.dp, 0.dp),
        maxLines = 1,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@ObsoleteCoroutinesApi
@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@ObsoleteCoroutinesApi
@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
