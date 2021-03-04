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
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
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
@ExperimentalAnimationApi
@Composable
fun MyApp() {
    Surface(color = MaterialTheme.colors.background) {
        var time by remember { mutableStateOf("00:00:00") }
        val hour = remember { mutableStateOf("00") }
        val minute = remember { mutableStateOf("00") }
        val second = remember { mutableStateOf("00") }
        var currentScreen by remember { mutableStateOf(TimerState.AWAITING_INPUT) }
        Box(modifier = Modifier.fillMaxSize()) {
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
                            label = "h"
                        )
                        TimeInputField(
                            value = minute,
                            maxValue = 60,
                            label = "m"
                        )
                        TimeInputField(
                            value = second,
                            maxValue = 60,
                            label = "s"
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
            AnimatedVisibility(
                visible = currentScreen == TimerState.AWAITING_INPUT,
                enter = expandIn() + fadeIn(),
                exit = shrinkOut() + fadeOut(),
                modifier = Modifier
                    .padding(PaddingValues(bottom = 32.dp))
                    .align(Alignment.BottomCenter),
                initiallyVisible = true
            ) {
                FloatingActionButton(
                    onClick = {
                        if (hour.value.isEmpty()) hour.value = "00"
                        if (minute.value.isEmpty()) minute.value = "00"
                        if (second.value.isEmpty()) second.value = "00"

                        val millisHour = TimeUnit.HOURS.toMillis(hour.value.toLong())
                        val millisMinute = TimeUnit.MINUTES.toMillis(minute.value.toLong())
                        val millisSecond = TimeUnit.SECONDS.toMillis(second.value.toLong())
                        object :
                            CountDownTimer(millisHour + millisMinute + millisSecond + 1000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                time =
                                    "${((millisUntilFinished / 1000) / 3600).padToDoubleHourUnit()}:${((millisUntilFinished / 1000) / 60).padToDoubleHourUnit()}:${((millisUntilFinished / 1000) % 60).padToDoubleHourUnit()}"
                            }

                            override fun onFinish() {
                                currentScreen = TimerState.AWAITING_INPUT
                            }
                        }.start()
                        currentScreen = TimerState.RUNNING
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Timer"
                    )
                }
            }
        }
    }
}

fun Long.padToDoubleHourUnit(): String = if (toString().length < 2) "0$this" else toString()

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

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp()
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp()
    }
}
