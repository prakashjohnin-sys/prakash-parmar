package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * શાળાનો માન્ય પરિપત્ર લોગો (School Official Seal Badge)
 * નગર પ્રાથમિક શિક્ષણ સમિતિ, આણંદ
 * નગર પ્રાથમિક શાળા નં - ૨૯ પુરુષોત્તમનગર બાકરોલ
 */
@Composable
fun SchoolLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    showDetails: Boolean = true
) {
    val crimsonRed = Color(0xFFBA1B1D)
    val goldYellow = Color(0xFFFFD54F)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(crimsonRed)
                .border(2.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner decorative ring
            Box(
                modifier = Modifier
                    .size(size * 0.86f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFFFFFDE7), Color(0xFFFFF8E1))
                        )
                    )
                    .border(1.5.dp, goldYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_school_logo),
                    contentDescription = "નગર પ્રાથમિક શાળા નં ૨૯ પુરુષોત્તમનગર લોગો",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
        }

        if (showDetails) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "સા વિદ્યા યા વિમુક્તયે",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = crimsonRed,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "ડાયસ કોડ : ૨૪૧૫૦૧૦૦૧૦૭",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4037)
            )
        }
    }
}
