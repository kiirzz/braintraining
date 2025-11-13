package com.braintraining.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            Text("Home")
        }
    ) { innerPadding ->
        Text("Recommend Game", modifier = Modifier.padding(innerPadding))
    }

}
