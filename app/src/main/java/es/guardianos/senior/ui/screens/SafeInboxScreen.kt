package es.guardianos.senior.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.guardianos.senior.data.MessageEntity
import es.guardianos.senior.data.MessageViewModel

@Composable
fun SafeInboxScreen(viewModel: MessageViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MENSAJES RECIBIDOS", style = MaterialTheme.typography.headlineMedium)
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(messages) { msg ->
                MessageCard(msg)
            }
        }

        if (messages.isEmpty()) {
            Text(
                "No hay mensajes recientes",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MessageCard(msg: MessageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(msg.sender.uppercase(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(msg.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(msg.timestamp), 
                 style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End)
        }
    }
}
