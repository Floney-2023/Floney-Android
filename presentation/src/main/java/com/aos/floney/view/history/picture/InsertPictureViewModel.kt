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
import com.aos.model.home.ImageUrls
import com.aos.model.home.PictureItem
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
import java.util.ArrayList
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

    private var _onSuccessImageUpload = MutableEventFlow<List<ImageUrls>>()
    val onSuccessImageUpload: EventFlow<List<ImageUrls>> get() = _onSuccessImageUpload

    private var _onClickPictureDetail = MutableEventFlow<String>()
    val onClickPictureDetail: EventFlow<String> get() = _onClickPictureDetail

    private var _onClickPictureDetailNum = MutableEventFlow<Int>()
    val onClickPictureDetailNum: EventFlow<Int> get() = _onClickPictureDetailNum

    private var _sortPictures = MutableEventFlow<List<PictureItem>>()
    val sortPictures: EventFlow<List<PictureItem>> get() = _sortPictures

    // 사진 촬영 uri
    private var takeCaptureUri: Uri? = null

    // 로컬 이미지 리스트 (File 형태)
    private var localImageList: MutableList<File> = mutableListOf()

    // S3 받은 url 리스트 (String 형태 - 서버에 보낼 값)
    private var cloudImageList = mutableListOf<ImageUrls>()

    // 이미지 업로드 정상 여부
    private var isSuccessImageUpload: Boolean = true

    // 몇번째 이미지 인지
    private var pictureNum = 0

    // 클라우드 이미지 리스트 세팅 (내용 수정 시)
    fun initPhotoList(originalUrl: List<ImageUrls>?) {
        originalUrl?.let {  cloudImageList = it.toMutableList() }

    }

    // 작성 저장하기 버튼 클릭
    fun onClickedSaveWriting() {
        baseEvent(Event.ShowLoading)
        viewModelScope.launch {

            Timber.e("localImageList $localImageList")

            withContext(Dispatchers.IO) {
                localImageList.forEach {
                    getPresignedUrl(it)
                }
            }

            baseEvent(Event.HideLoading)
            Timber.e("pictureUrlList $cloudImageList")
            if (isSuccessImageUpload) {
                _onSuccessImageUpload.emit(cloudImageList)
            }
        }
    }

    // 사진 상세보기 클릭
    fun onClickedPictureDetail(num: Int) {
        // num 번째 파일의 url를 읽어온다.
        // 클라우드 이미지 리스트 -> 로컬 이미지 리스트 순서대로 접근한다.
        // 몇 번쨰 이미지 값인 지 보낸다.
        viewModelScope.launch {
            _onClickPictureDetailNum.emit(num)
                /*
            getImageFile(num)?.let {
                _onClickPictureDetail.emit(it.absolutePath)
            }*/
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
                    isSuccessImageUpload = true
                    cloudImageList.add(ImageUrls(-1,viewUrl))
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

    fun deletePictureFile(imageUrls: ImageUrls) {
        viewModelScope.launch {
            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    if (imageUrls.id != -1) {
                        // 클라우드 리스트에서 삭제
                        cloudImageList.removeAll { it == imageUrls }
                    } else {
                        // 로컬 리스트에서 삭제
                        localImageList.removeAll { it.absolutePath == imageUrls.url }
                    }
                }

                // ✅ 클라우드 리스트를 PictureItem.CloudImage로 변환 + 로컬 리스트를 PictureItem.LocalImage로 변환
                val sortedList: List<PictureItem> =  cloudImageList.map { PictureItem.CloudImage(ImageUrls(-1, it.url)) } +  // S3 URL 리스트 변환
                            localImageList.map { PictureItem.LocalImage(it) }        // 로컬 파일 리스트 변환

                _sortPictures.emit(sortedList)
            }
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
                localImageList.add(file)
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

    fun getPictureList(): List<ImageUrls> {
        return cloudImageList
    }

    fun setPictureNum(num: Int) {
        pictureNum = num
    }

    // 파일 불러오기
    fun getImageFile(num: Int): File? {
        val cloudImageSize = cloudImageList.size
        return if (localImageList.size+cloudImageSize>= num) localImageList[num - cloudImageSize - 1] else null
    }

    // 로컬 파일 리스트 불러오기
    fun getImageFileList(): List<File> {
        return localImageList
    }

    // 클라우드 이미지 리스트 불러오기
    fun getCloudFileList(): List<ImageUrls> {
        return cloudImageList
    }
}