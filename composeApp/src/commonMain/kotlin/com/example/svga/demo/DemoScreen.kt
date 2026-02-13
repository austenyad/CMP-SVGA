package com.example.svga.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.yad.svga.compose.SVGAImage
import com.yad.svga.compose.SVGASpec

private val demoAssets = listOf("angel.svga", "heartbeat.svga", "rose.svga")

@Composable
fun DemoScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val spec = SVGASpec.Asset(demoAssets[selectedIndex])
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CMP-SVGA Demo",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(top = 48.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            demoAssets.forEachIndexed { index, name ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    label = { Text(name.removeSuffix(".svga")) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── With explicit size modifier (red border) ──
        Text("With modifier:", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
        SVGAImage(
            spec = spec,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .aspectRatio(1f)
                .border(2.dp, Color.Red),
            contentScale = ContentScale.Fit,
            loading = {
                CircularProgressIndicator(color = Color.White)
            },
            failure = { error ->
                Text("Error: $error", color = Color.Red)
            }
        )

        Spacer(Modifier.height(16.dp))

        // ── Without modifier (green border, default size from videoSize) ──
        Text("Without modifier:", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
        SVGAImage(
            spec = spec,
            modifier = Modifier.border(2.dp, Color.Green),
            contentScale = ContentScale.Fit,
            loading = {
                CircularProgressIndicator(color = Color.White)
            },
            failure = { error ->
                Text("Error: $error", color = Color.Red)
            }
        )

        Spacer(Modifier.height(32.dp))
    }
}
