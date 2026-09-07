package com.example.tlstunnelmvp

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        criarInterface()
    }

    private fun criarInterface() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 40, 30, 30)
        layout.gravity = Gravity.CENTER_HORIZONTAL

        val titulo = TextView(this)
        titulo.text = "TLS Tunnel MVP"
        titulo.textSize = 28f
        titulo.setTextColor(Color.WHITE)
        titulo.gravity = Gravity.CENTER
        titulo.setPadding(0, 0, 0, 30)

        val subtitulo = TextView(this)
        subtitulo.text = "Conexão com Internet"
        subtitulo.textSize = 17f
        subtitulo.setTextColor(Color.LTGRAY)
        subtitulo.gravity = Gravity.CENTER

        statusText = TextView(this)
        statusText.text = "Status: desconectado"
        statusText.textSize = 18f
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 40, 0, 20)

        progressBar = ProgressBar(this)
        progressBar.visibility = View.GONE

        val testar = Button(this)
        testar.text = "TESTAR INTERNET"
        testar.setOnClickListener {
            testarInternet()
        }

        val conectar = Button(this)
        conectar.text = "CONECTAR"
        conectar.setOnClickListener {
            conectar()
        }

        val desconectar = Button(this)
        desconectar.text = "DESCONECTAR"
        desconectar.setOnClickListener {
            desconectar()
        }

        val informacoes = Button(this)
        informacoes.text = "INFORMAÇÕES"
        informacoes.setOnClickListener {
            mostrarInformacoes()
        }

        val sair = Button(this)
        sair.text = "SAIR"
        sair.setOnClickListener {
            finish()
        }

        layout.addView(titulo)
        layout.addView(subtitulo)
        layout.addView(statusText)
        layout.addView(progressBar)

        layout.addView(
            testar,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            conectar,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            desconectar,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            informacoes,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            sair,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.setBackgroundColor(Color.rgb(20, 20, 25))

        setContentView(layout)
    }

    private fun testarInternet() {

        statusText.text = "Status: testando Internet..."
        progressBar.visibility = View.VISIBLE

        thread {

            var conectado = false

            try {
                val url = URL("https://www.google.com")

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                connection.connect()

                conectado = connection.responseCode in 200..399

                connection.disconnect()

            } catch (e: Exception) {
                conectado = false
            }

            mainHandler.post {

                progressBar.visibility = View.GONE

                if (conectado) {
                    statusText.text = "Status: Internet funcionando ✓"
                    Toast.makeText(
                        this,
                        "Conexão com a Internet OK",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    statusText.text = "Status: sem conexão com a Internet"
                    Toast.makeText(
                        this,
                        "Não foi possível acessar a Internet",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun conectar() {

        statusText.text = "Status: conectado ✓"

        Toast.makeText(
            this,
            "Aplicativo conectado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun desconectar() {

        statusText.text = "Status: desconectado"

        Toast.makeText(
            this,
            "Conexão encerrada",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun mostrarInformacoes() {

        AlertDialog.Builder(this)
            .setTitle("TLS Tunnel MVP")
            .setMessage(
                "Aplicativo de teste de conexão.\n\n" +
                "Internet: habilitada\n" +
                "HTTPS: habilitado\n" +
                "Teste de conexão: disponível"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
