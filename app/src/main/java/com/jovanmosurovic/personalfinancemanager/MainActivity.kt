package com.jovanmosurovic.personalfinancemanager

import android.os.Bundle
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jovanmosurovic.personalfinancemanager.ui.PersonalFinanceApp
import com.jovanmosurovic.personalfinancemanager.ui.theme.PersonalfinancemanagerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            PersonalfinancemanagerTheme {
                PersonalFinanceApp()
            }
        }
    }
}
