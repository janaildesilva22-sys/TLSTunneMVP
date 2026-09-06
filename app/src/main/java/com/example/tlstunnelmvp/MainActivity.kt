package com.example.tlstunnelmvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TLSTunnelApp()
        }
    }
}

@Composable
fun TLSTunnelApp() {

    var connected by remember {
        mutableStateOf(false)
    }

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "TLS Tunnel MVP",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = if (connected) {
                        "Status: CONECTADO"
                    } else {
                        "Status: DESCONECTADO"
                    }
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Button(
                    onClick = {
                        connected = !connected
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (connected) {
                            "DESCONECTAR"
                        } else {
                            "CONECTAR"
                        }
                    )
                }
            }
        }
    }
}
