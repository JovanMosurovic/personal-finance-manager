package com.jovanmosurovic.personalfinancemanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jovanmosurovic.personalfinancemanager.ui.PersonalFinanceApp
import com.jovanmosurovic.personalfinancemanager.ui.theme.PersonalfinancemanagerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalfinancemanagerTheme {
                PersonalFinanceApp()
            }
        }
    }
}
