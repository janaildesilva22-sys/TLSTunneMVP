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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var pendingHost = "tlstunnemvp.fly.dev"
    private var pendingPort = 443

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
        mutableStateOf("tlstunnemvp.fly.dev")
    }

    var port by remember {
        mutableStateOf("443")
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
                    text = "TLS Tunnel",
                    style =
                        MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Secure connection",
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "STATUS",
                            style =
                                MaterialTheme.typography.labelMedium,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                if (connected)
                                    "● CONECTADO"
                                else
                                    "● DESCONECTADO",

                            style =
                                MaterialTheme.typography.titleLarge,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                if (connected)
                                    "Túnel em execução"
                                else
                                    "Pronto para conectar"
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
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
                        Modifier.fillMaxWidth(),

                    enabled = !connected
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
                        Modifier.fillMaxWidth(),

                    enabled = !connected
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
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
                                host.isNotBlank() &&
                                portNumber != null &&
                                portNumber in 1..65535
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
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                    shape =
                        RoundedCornerShape(16.dp)
                ) {

                    Text(
                        text =
                            if (connected)
                                "DESCONECTAR"
                            else
                                "CONECTAR",

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    TextButton(
                        onClick = {
                            host = "tlstunnemvp.fly.dev"
                            port = "443"
                        }
                    ) {

                        Text("Restaurar padrão")
                    }

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            connected = false
                        }
                    ) {

                        Text("Limpar")
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(
                    text = "TLS Tunnel MVP • v0.1",
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
