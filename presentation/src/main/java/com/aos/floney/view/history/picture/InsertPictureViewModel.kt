package com.aos.floney.view.history.picture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.subscribe.SubscribePresignedUrlUseCase
import com.letspl.oceankeeper.util.ImgFileMaker
import com.letspl.oceankeeper.util.RotateTransform
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class InsertPictureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferenceUtil,
    private val subscribePresignedUrlUseCase: SubscribePresignedUrlUseCase
) : BaseViewModel() {

    private var _onClickedSaveWriting = MutableEventFlow<Boolean>()
    val onClickedSaveWriting: EventFlow<Boolean> get() = _onClickedSaveWriting

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedAddPicture = MutableEventFlow<Boolean>()
    val onClickedAddPicture: EventFlow<Boolean> get() = _onClickedAddPicture

    private var _onSuccessImageUpload = MutableEventFlow<List<String>>()
    val onSuccessImageUpload: EventFlow<List<String>> get() = _onSuccessImageUpload

    private var _onClickPictureDetail = MutableEventFlow<String>()
    val onClickPictureDetail: EventFlow<String> get() = _onClickPictureDetail

    private var _sortPictures = MutableEventFlow<List<File>>()
    val sortPictures: EventFlow<List<File>> get() = _sortPictures

    // 사진 촬영 uri
    private var takeCaptureUri: Uri? = null
    private var imageFileList: MutableList<File> = mutableListOf()

    // S3 받은 url 리스트
    private val pictureUrlList = mutableListOf<String>()

    // 이미지 업로드 정상 여부
    private var isSuccessImageUpload: Boolean = true

    // 몇번째 이미지 인지
    private var pictureNum = 0

    // 작성 저장하기 버튼 클릭
    fun onClickedSaveWriting() {
        baseEvent(Event.ShowLoading)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                imageFileList.forEach {
                    getPresignedUrl(it)
                }
            }

            baseEvent(Event.HideLoading)
            Timber.e("pictureUrlList $pictureUrlList")
            if (isSuccessImageUpload) {
                _onSuccessImageUpload.emit(pictureUrlList)
            }
        }
    }

    // 사진 상세보기 클릭
    fun onClickedPictureDetail(num: Int) {
        viewModelScope.launch {
            getImageFile(num)?.let {
                _onClickPictureDetail.emit(it.absolutePath)
            }
        }
    }

    fun onClickedBack() {
        viewModelScope.launch {
            _onClickedBack.emit(true)
        }
    }

    fun onClickedAddPicture() {
        viewModelScope.launch {
            _onClickedAddPicture.emit(true)
        }
    }

    fun addPictureNum() {
        pictureNum++
    }

    // 사진 촬영을 위해 임시 파일 생성
    fun createTempImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            content
        )
    }

    // 회전 각도를 맞춘 이미지 파일 생성
    fun createBitmapFile(uri: Uri?): Bitmap? {
        return if (uri != null) {
            val path =
                ImgFileMaker.getFullPathFromUri(context, uri)!!
            val angle = RotateTransform.getRotationAngle(path)
            val bitmap = RotateTransform.rotateImage(
                context,
                BitmapFactory.decodeFile(path),
                angle.toFloat(),
                uri
            )
            if (bitmap != null) {
                setImageBitmap(bitmap)
                bitmap
            } else {
                baseEvent(Event.ShowToast("이미지 파일 생성에 실패하였습니다."))
                null
            }
        } else {
            baseEvent(Event.ShowToast("이미지 파일 설정에 실패하였습니다."))
            null
        }
    }

    private suspend fun getPresignedUrl(file: File) {
        subscribePresignedUrlUseCase(prefs.getString("bookKey", "")).onSuccess {
            Timber.e("it $it")
            val url = it.url
            uploadFileToPresignedUrl(url, file, it.viewUrl)
        }.onFailure {
            baseEvent(Event.HideLoading)
            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@InsertPictureViewModel)))
            isSuccessImageUpload = false
        }
    }

    private suspend fun uploadFileToPresignedUrl(
        presignedUrl: String,
        file: File,
        viewUrl: String
    ) {
        try {
            withContext(Dispatchers.IO) {
                val url = URL(presignedUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "image/jpeg") // 파일 형식에 맞게 변경

                file.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (connection.responseCode == 200) {
                    println("File uploaded successfully!")
                    pictureUrlList.add(viewUrl)
                } else {
                    println("Upload failed with response code: ${connection.responseCode}")
                    baseEvent(Event.ShowToast(connection.responseMessage.parseErrorMsg(this@InsertPictureViewModel)))
                    isSuccessImageUpload = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccessImageUpload = false
        }
    }

    private fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): File? {
        return try {
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            isSuccessImageUpload = false
            null
        }
    }

    fun deletePictureFile(path: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val removeItem = imageFileList.filter { it.absolutePath == path }

                imageFileList.removeAll(removeItem)
            }

            _sortPictures.emit(imageFileList)
        }
    }

    // 임시 촬영 uri 저장
    fun setTakeCaptureUri(uri: Uri?) {
        takeCaptureUri = uri
    }

    // 임시 촬영 uri 불러오기
    fun getTakeCaptureUri(): Uri? {
        return takeCaptureUri
    }

    // 임시 촬영 파일 저장
    private fun setImageBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            saveBitmapToTempFile(context, cropBitmapToSquare(it))?.let { file ->
                imageFileList.add(file)
            }
        }
    }

    private fun cropBitmapToSquare(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    fun getPictureNum(): Int {
        return pictureNum
    }

    fun getPictureList(): List<String> {
        return pictureUrlList
    }

    fun setPictureNum(num: Int) {
        pictureNum = num
    }

    // 파일 불러오기
    fun getImageFile(num: Int): File? {
        return if (imageFileList.size >= num) imageFileList[num - 1] else null
    }

    // 파일 리스트 불러오기
    fun getImageFileList(): List<File> {
        return imageFileList
    }

}