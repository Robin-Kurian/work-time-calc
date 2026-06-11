package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.NavigationTab
import com.example.ui.theme.AppTheme
import com.example.ui.theme.glassSurface

@Composable
fun AppBottomBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassSurface(
                shape = RoundedCornerShape(24.dp),
                fillAlpha = 0.72f,
                elevation = 10.dp
            ),
        containerColor = Color.Transparent,
        contentColor = AppTheme.colors.mutedText,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.Today,
            onClick = { onTabSelected(NavigationTab.Today) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Today,
                    contentDescription = "Today tab"
                )
            },
            label = {
                Text(
                    text = NavigationTab.Today.label,
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == NavigationTab.Today) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = navigationBarItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.Plan,
            onClick = { onTabSelected(NavigationTab.Plan) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Keyboard,
                    contentDescription = "Plan tab"
                )
            },
            label = {
                Text(
                    text = NavigationTab.Plan.label,
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == NavigationTab.Plan) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = navigationBarItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == NavigationTab.Focus,
            onClick = { onTabSelected(NavigationTab.Focus) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = "Focus tab"
                )
            },
            label = {
                Text(
                    text = NavigationTab.Focus.label,
                    fontSize = 10.sp,
                    fontWeight = if (selectedTab == NavigationTab.Focus) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = navigationBarItemColors()
        )
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppTheme.colors.accentGreen,
    selectedTextColor = AppTheme.colors.accentGreen,
    unselectedIconColor = AppTheme.colors.mutedText,
    unselectedTextColor = AppTheme.colors.mutedText,
    indicatorColor = AppTheme.colors.accentGreenBg
)
