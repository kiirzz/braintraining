package com.braintraining.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AccountScreen(
    navController: NavHostController
) {
    LazyColumn() {
        item { AccountTopBar(navController) }
        item {
            Text("Account Screen")
        }
    }
}

@Composable
fun AccountTopBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Account", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Back")
        }
    }

}
