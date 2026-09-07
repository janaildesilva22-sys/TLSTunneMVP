package com.example.tlstunnelmvp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    mutableStateOf("tlstunnemvp.fly.dev")
    private var pendingPort = 4433

    private val vpnPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                startTunnelService(
                    pendingHost,
                    pendingPort
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            TLSTunnelApp(

                onConnect = { host, port ->

                    pendingHost = host
                    pendingPort = port

                    val prepareIntent =
                        VpnService.prepare(this)

                    if (prepareIntent != null) {

                        vpnPermissionLauncher.launch(
                            prepareIntent
                        )

                    } else {

                        startTunnelService(
                            host,
                            port
                        )
                    }
                },

                onDisconnect = {

                    stopService(
                        Intent(
                            this,
                            TunnelVpnService::class.java
                        )
                    )
                }
            )
        }
    }

    private fun startTunnelService(
        host: String,
        port: Int
    ) {

        val intent =
            Intent(
                this,
                TunnelVpnService::class.java
            )

        intent.putExtra(
            TunnelVpnService.EXTRA_HOST,
            host
        )

        intent.putExtra(
            TunnelVpnService.EXTRA_PORT,
            port
        )

        ContextCompat.startForegroundService(
            this,
            intent
        )
    }
}

@Composable
fun TLSTunnelApp(
    onConnect: (String, Int) -> Unit,
    onDisconnect: () -> Unit
) {

    var connected by remember {
        mutableStateOf(false)
    }

    var host by remember {
        mutableStateOf("127.0.0.1")
    }

    var port by remember {
        mutableStateOf("4433")
    }

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(
                    text = "TLS Tunnel MVP",
                    style =
                        MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text =
                        if (connected)
                            "Status: CONECTADO"
                        else
                            "Status: DESCONECTADO"
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                    },
                    label = {
                        Text("Servidor")
                    },
                    singleLine = true,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = {
                        port = it
                    },
                    label = {
                        Text("Porta")
                    },
                    singleLine = true,
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Button(
                    onClick = {

                        if (connected) {

                            onDisconnect()
                            connected = false

                        } else {

                            val portNumber =
                                port.toIntOrNull()

                            if (
                                portNumber != null &&
                                portNumber in 1..65535 &&
                                host.isNotBlank()
                            ) {

                                onConnect(
                                    host,
                                    portNumber
                                )

                                connected = true
                            }
                        }

                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            if (connected)
                                "DESCONECTAR"
                            else
                                "CONECTAR"
                    )
                }
            }
        }
    }
}
