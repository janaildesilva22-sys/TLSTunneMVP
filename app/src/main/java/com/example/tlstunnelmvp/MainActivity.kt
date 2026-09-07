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

    private lateinit var status: TextView
    private lateinit var progresso: ProgressBar

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        criarTela()
    }

    private fun criarTela() {

        val tela = LinearLayout(this)

        tela.orientation = LinearLayout.VERTICAL
        tela.gravity = Gravity.CENTER_HORIZONTAL
        tela.setPadding(30, 40, 30, 30)
        tela.setBackgroundColor(Color.rgb(20, 20, 25))

        val titulo = TextView(this)

        titulo.text = "TLS Tunnel MVP"
        titulo.textSize = 28f
        titulo.setTextColor(Color.WHITE)
        titulo.gravity = Gravity.CENTER
        titulo.setPadding(0, 0, 0, 20)

        tela.addView(titulo)

        val descricao = TextView(this)

        descricao.text = "Aplicativo de conexão"
        descricao.textSize = 17f
        descricao.setTextColor(Color.LTGRAY)
        descricao.gravity = Gravity.CENTER

        tela.addView(descricao)

        status = TextView(this)

        status.text = "Status: desconectado"
        status.textSize = 18f
        status.setTextColor(Color.WHITE)
        status.gravity = Gravity.CENTER
        status.setPadding(0, 35, 0, 20)

        tela.addView(status)

        progresso = ProgressBar(this)

        progresso.visibility = View.GONE

        tela.addView(progresso)

        val testar = Button(this)

        testar.text = "TESTAR INTERNET"

        testar.setOnClickListener {
            testarInternet()
        }

        tela.addView(
            testar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val conectar = Button(this)

        conectar.text = "CONECTAR"

        conectar.setOnClickListener {
            conectar()
        }

        tela.addView(
            conectar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val desconectar = Button(this)

        desconectar.text = "DESCONECTAR"

        desconectar.setOnClickListener {
            desconectar()
        }

        tela.addView(
            desconectar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val informacoes = Button(this)

        informacoes.text = "INFORMAÇÕES"

        informacoes.setOnClickListener {
            informacoes()
        }

        tela.addView(
            informacoes,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val sair = Button(this)

        sair.text = "SAIR"

        sair.setOnClickListener {
            finish()
        }

        tela.addView(
            sair,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(tela)
    }

    private fun testarInternet() {

        status.text = "Status: verificando Internet..."
        progresso.visibility = View.VISIBLE

        thread {

            var sucesso = false

            try {

                val url = URL("https://www.google.com")

                val conexao =
                    url.openConnection() as HttpURLConnection

                conexao.requestMethod = "GET"
                conexao.connectTimeout = 10000
                conexao.readTimeout = 10000

                conexao.connect()

                sucesso = conexao.responseCode in 200..399

                conexao.disconnect()

            } catch (erro: Exception) {

                sucesso = false
            }

            handler.post {

                progresso.visibility = View.GONE

                if (sucesso) {

                    status.text =
                        "Status: Internet funcionando ✓"

                    Toast.makeText(
                        this,
                        "Internet funcionando",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    status.text =
                        "Status: Internet indisponível"

                    Toast.makeText(
                        this,
                        "Falha ao acessar a Internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun conectar() {

        status.text = "Status: conectado ✓"

        Toast.makeText(
            this,
            "Conectado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun desconectar() {

        status.text = "Status: desconectado"

        Toast.makeText(
            this,
            "Desconectado",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun informacoes() {

        AlertDialog.Builder(this)
            .setTitle("TLS Tunnel MVP")
            .setMessage(
                "Acesso à Internet: habilitado\n\n" +
                "Teste HTTPS: disponível\n\n" +
                "Versão: MVP"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }
}
