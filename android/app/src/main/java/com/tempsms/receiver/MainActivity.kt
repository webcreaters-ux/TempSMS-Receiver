package com.tempsms.receiver

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

data class SmsMessage(val id:String,val number:String,val sender:String,val text:String,val receivedAt:String,val otp:String?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TempSmsApp() }
    }
}

@Composable
fun TempSmsApp() {
    var apiUrl by remember { mutableStateOf("http://10.0.2.2:8080") }
    var token by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<SmsMessage>()) }
    var showSettings by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title={Text("TempSMS Receiver")}, actions={
                TextButton(onClick={showSettings=!showSettings}) { Text("Settings") }
            }) }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                if (showSettings) {
                    Text("Authorized provider API", style=MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(apiUrl,{apiUrl=it},label={Text("Server URL")},modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(token,{token=it},label={Text("App token")},modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Text("The token is for your private server. Provider credentials stay on the server.")
                } else {
                    Text("Inbox", style=MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick={/* network layer added next */}, modifier=Modifier.fillMaxWidth()) { Text("Refresh messages") }
                    Spacer(Modifier.height(12.dp))
                    if (messages.isEmpty()) Text("No messages yet. Configure an authorized SMS-provider webhook.")
                    LazyColumn { items(messages) { m ->
                        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(m.sender, style=MaterialTheme.typography.titleMedium)
                                Text(m.number)
                                Text(m.text)
                                m.otp?.let { Text("OTP: $it", style=MaterialTheme.typography.titleLarge) }
                            }
                        }
                    }}
                }
            }
        }
    }
}
