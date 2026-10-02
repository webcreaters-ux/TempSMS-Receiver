package com.tempsms.receiver

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class SmsMessage(val id:String,val number:String,val sender:String,val text:String,val receivedAt:String,val otp:String?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TempSmsApp(this) }
    }
}

private fun loadMessages(baseUrl:String, token:String): List<SmsMessage> {
    val connection = (URL(baseUrl.trimEnd('/') + "/api/messages").openConnection() as HttpURLConnection)
    connection.requestMethod = "GET"
    connection.setRequestProperty("Authorization", "Bearer $token")
    connection.connectTimeout = 8000
    connection.readTimeout = 8000
    return try {
        if (connection.responseCode !in 200..299) error("HTTP " + connection.responseCode)
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(body)
        buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(SmsMessage(o.getString("id"), o.getString("number"), o.optString("sender"), o.getString("text"), o.getString("receivedAt"), o.optString("otp").ifBlank { null }))
            }
        }
    } finally { connection.disconnect() }
}

@Composable
fun TempSmsApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("config", Context.MODE_PRIVATE) }
    var apiUrl by remember { mutableStateOf(prefs.getString("url","http://10.0.2.2:8080") ?: "") }
    var token by remember { mutableStateOf(prefs.getString("token","") ?: "") }
    var messages by remember { mutableStateOf(emptyList<SmsMessage>()) }
    var status by remember { mutableStateOf("Ready") }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    MaterialTheme {
        Scaffold(topBar={TopAppBar(title={Text("TempSMS Receiver")}, actions={TextButton(onClick={showSettings=!showSettings}) { Text("Settings") }})}) { padding ->
            Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                if (showSettings) {
                    Text("Authorized provider server", style=MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(apiUrl,{apiUrl=it},label={Text("Server URL")},modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(token,{token=it},label={Text("App token")},modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick={prefs.edit().putString("url",apiUrl).putString("token",token).apply(); status="Settings saved"}) { Text("Save") }
                    Spacer(Modifier.height(12.dp))
                    Text("Provider API keys belong on the server, never inside this APK.")
                } else {
                    Text("Inbox", style=MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick={
                        if (token.isBlank()) { status="Set your app token in Settings"; return@Button }
                        status="Refreshing…"
                        scope.launch(Dispatchers.IO) {
                            runCatching { loadMessages(apiUrl,token) }.onSuccess { result -> messages=result; status=result.size.toString()+" message(s)" }.onFailure { status="Refresh failed: "+it.message }
                        }
                    }, modifier=Modifier.fillMaxWidth()) { Text("Refresh messages") }
                    Spacer(Modifier.height(8.dp))
                    Text(status)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn { items(messages) { m ->
                        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) { Column(Modifier.padding(12.dp)) {
                            Text(m.sender.ifBlank{"Unknown sender"}, style=MaterialTheme.typography.titleMedium)
                            Text(m.number); Text(m.text)
                            m.otp?.let { Text("OTP: "+it, style=MaterialTheme.typography.titleLarge) }
                            Text(m.receivedAt, style=MaterialTheme.typography.bodySmall)
                        }}
                    }}
                }
            }
        }
    }
}