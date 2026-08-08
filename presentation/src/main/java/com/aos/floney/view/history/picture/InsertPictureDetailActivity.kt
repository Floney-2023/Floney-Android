package com.aos.floney.view.history.picture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.aos.floney.ext.intentSerializableList
import com.aos.model.home.ImageUrls
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsertPictureDetailActivity : ComponentActivity() {

    private val viewModel: InsertPictureDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val images = intent.intentSerializableList<ImageUrls>("imageList")
        val startIndex = intent.getIntExtra("startIndex", 0)
        viewModel.setImageList(images, startIndex)

        setContent {
            InsertPictureDetailScreen(
                images = images,
                startIndex = startIndex,
                onBack = { finish() },
                onDelete = { currentPage ->
                    val result = Intent()
                    result.putExtra("deleteFilePath", viewModel.getImage(currentPage))
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            )
        }
    }
}
