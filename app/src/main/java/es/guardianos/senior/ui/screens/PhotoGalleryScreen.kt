package es.guardianos.senior.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGalleryScreen(photoUris: List<Uri>) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { photoUris.size })
    val scope = rememberCoroutineScope()
    var autoPlay by remember { mutableStateOf(false) }

    LaunchedEffect(autoPlay) {
        while (autoPlay) {
            delay(5000L)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % photoUris.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FOTOS FAMILIA", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { autoPlay = !autoPlay }) {
                Text(if (autoPlay) "⏸️ PAUSAR" else "▶️ AUTO")
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            ZoomableImage(uri = photoUris[page])
        }
    }
}

@Composable
fun ZoomableImage(uri: Uri) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = "Foto familiar",
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .graphicsLayer(
                scaleX = scale, scaleY = scale,
                translationX = offsetX, translationY = offsetY
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f; offsetY = 0f
                    }
                }
            }
    )
}
