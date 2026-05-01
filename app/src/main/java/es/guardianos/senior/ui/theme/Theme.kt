package es.guardianos.senior.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF03A9F4),
    error = Color(0xFFFF5252),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

@Composable
fun GuardianOSSeniorTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        headlineMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
        bodyLarge = TextStyle(fontSize = 24.sp),
        labelLarge = TextStyle(fontSize = 22.sp)
    )
    MaterialTheme(colorScheme = DarkColorScheme, typography = typography, content = content)
}
