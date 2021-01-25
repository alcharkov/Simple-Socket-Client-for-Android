package lt.alcharkov.simple_socket_client

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.InetSocketAddress

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<View>(R.id.button) as Button
        val inputText = findViewById<View>(R.id.inputText) as EditText
        val outputText = findViewById<View>(R.id.outputText) as EditText
        val addressText = findViewById<View>(R.id.addressText) as EditText

        val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = resources.getString(R.string.server_address_default_key)
        val highScore = sharedPref.getString(getString(R.string.server_address), defaultValue)
        addressText.setText(highScore)


        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent)
                }
            }
        }

        button.setOnClickListener {
            with (sharedPref.edit()) {
                putString(getString(R.string.server_address), addressText.text.toString())
                apply()
            }

            CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val addressArr = addressText.text.toString().split(":")
                        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress(addressArr[0], Integer.parseInt(addressArr[1])))
                        val input = socket.openReadChannel()
                        val output = socket.openWriteChannel(autoFlush = true)

                        output.writeStringUtf8(inputText.text)
                        val response = input.readUTF8Line()
                        outputTextToWidget(outputText, response, "Server")
                    } catch (e: Exception) {
                        outputTextToWidget(outputText, e.message, "Client")
                    }

            }
        }
    }

    private fun outputTextToWidget(outputText: EditText, response: String?, consumer: String) {
        if (outputText.text.isBlank()) {
            outputText.append("$consumer: $response")
        } else {
            outputText.append("\n$consumer : $response")
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            val inputText = findViewById<View>(R.id.inputText) as EditText
            inputText.setText(it)
        }
    }
}