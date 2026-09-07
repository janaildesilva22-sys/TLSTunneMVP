package com.example.tlstunnelmvp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

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
                        vpnPermissionLauncher.launch(prepareIntent)
                    } else {
                        startTunnelService(host, port)
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

    var status by remember {
        mutableStateOf("Pronto para conectar")
    }

    var testing by remember {
        mutableStateOf(false)
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(20.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )

                Text(
                    text = "TLS Tunnel",
                    style =
                        MaterialTheme.typography.headlineLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = "MVP",
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp),
                    colors =
                        CardDefaults.cardColors()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "STATUS",
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
                                    "🟢 CONECTADO"
                                else
                                    "⚪ DESCONECTADO",
                            style =
                                MaterialTheme.typography.titleLarge,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text = status
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
                        if (
                            it.length <= 5 &&
                            it.all { char ->
                                char.isDigit()
                            }
                        ) {
                            port = it
                        }
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
                        Modifier.height(18.dp)
                )

                Button(
                    onClick = {

                        if (connected) {

                            onDisconnect()

                            connected = false

                            status =
                                "Túnel desconectado"

                        } else {

                            val portNumber =
                                port.toIntOrNull()

                            if (
                                host.isBlank()
                            ) {

                                status =
                                    "Digite o servidor"

                            } else if (
                                portNumber == null ||
                                portNumber !in 1..65535
                            ) {

                                status =
                                    "Porta inválida"

                            } else {

                                status =
                                    "Iniciando conexão..."

                                onConnect(
                                    host,
                                    portNumber
                                )

                                connected = true
                            }
                        }
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors()
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
                        Modifier.height(10.dp)
                )

                Button(
                    onClick = {

                        if (testing) {
                            return@Button
                        }

                        val portNumber =
                            port.toIntOrNull()

                        if (
                            host.isBlank() ||
                            portNumber == null ||
                            portNumber !in 1..65535
                        ) {

                            status =
                                "Servidor ou porta inválidos"

                            return@Button
                        }

                        testing = true

                        status =
                            "Testando servidor..."

                        Thread {

                            var socket:
                                    SSLSocket? = null

                            try {

                                val factory =
                                    SSLSocketFactory.getDefault()
                                        as SSLSocketFactory

                                socket =
                                    factory.createSocket()
                                        as SSLSocket

                                socket.soTimeout =
                                    8000

                                socket.connect(
                                    InetSocketAddress(
                                        host,
                                        portNumber
                                    ),
                                    8000
                                )

                                socket.startHandshake()

                                runOnUiThread {

                                    status =
                                        "Servidor TLS ONLINE ✓"

                                    testing = false
                                }

                            } catch (
                                error: Exception
                            ) {

                                runOnUiThread {

                                    status =
                                        "Falha: servidor não respondeu"

                                    testing = false
                                }

                            } finally {

                                try {
                                    socket?.close()
                                } catch (
                                    _: Exception
                                ) {
                                }
                            }

                        }.start()
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(14.dp),
                    enabled =
                        !testing,
                    colors =
                        ButtonDefaults.buttonColors()
                ) {

                    Text(
                        text =
                            if (testing)
                                "TESTANDO..."
                            else
                                "TESTAR SERVIDOR"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(
                        onClick = {

                            val prefs =
                                context.getSharedPreferences(
                                    "tls_tunnel",
                                    android.content.Context.MODE_PRIVATE
                                )

                            prefs.edit()
                                .putString(
                                    "host",
                                    host
                                )
                                .putString(
                                    "port",
                                    port
                                )
                                .apply()

                            status =
                                "Configuração salva ✓"
                        },
                        modifier =
                            Modifier.weight(1f),
                        shape =
                            RoundedCornerShape(14.dp)
                    ) {

                        Text("SALVAR")
                    }

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    OutlinedButton(
                        onClick = {

                            host =
                                "tlstunnemvp.fly.dev"

                            port =
                                "443"

                            status =
                                "Configuração restaurada"
                        },
                        modifier =
                            Modifier.weight(1f),
                        shape =
                            RoundedCornerShape(14.dp)
                    ) {

                        Text("PADRÃO")
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(18.dp),
                    colors =
                        CardDefaults.cardColors()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(18.dp)
                    ) {

                        Text(
                            text = "INFORMAÇÕES",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Protocolo: TLSTUNNEL-MVP/2"
                        )

                        Text(
                            text =
                                "Transporte: TLS"
                        )

                        Text(
                            text =
                                "Porta externa: 443"
                        )

                        Text(
                            text =
                                "Backend: Fly.io"
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )

                Text(
                    text =
                        "TLS Tunnel MVP • v0.1.0",
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )
            }
        }
    }
}
