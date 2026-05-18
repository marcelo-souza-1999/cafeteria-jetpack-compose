package com.targaryen.cafeteria.core_designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.targaryen.cafeteria.core_designsystem.theme.DimmedGold
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargaryenTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = DimmedGold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = ValyrianGold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Obsidian
        ),
        modifier = modifier
    )
}
