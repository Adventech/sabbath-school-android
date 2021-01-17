package com.cryart.sabbathschool.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cryart.sabbathschool.lessons.databinding.ActivityLessonsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LessonsActivity : AppCompatActivity() {

    private val viewModel: LessonsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLessonsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
