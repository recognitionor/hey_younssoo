package com.bium.youngssoo.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import youngsso.composeapp.generated.resources.Res
import youngsso.composeapp.generated.resources.sf_pro
import org.jetbrains.compose.resources.Font

@Composable
fun BeumButton(modifier: Modifier, text: String) {
    Box(
        modifier = modifier.background(
            color = BeumColors.angelSkyblue,
            shape = RoundedCornerShape(size = BeumDimen.radius100)
        ).clip(shape = RoundedCornerShape(size = BeumDimen.radius100)).clickable {

        }.padding(
            start = BeumDimen.Px15RemSpacing09,
            top = 0.dp,
            end = BeumDimen.Px15RemSpacing09,
            bottom = 0.dp
        ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, style = TextStyle(
                fontSize = BeumTypo.TypoScaleText300,
                fontFamily = FontFamily(Font(Res.font.sf_pro)),
                fontWeight = FontWeight(700),
                color = BeumColors.White,
                textAlign = TextAlign.Center,
            )
        )
    }
}