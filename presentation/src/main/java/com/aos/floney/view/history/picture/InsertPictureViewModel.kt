package com.aos.floney.view.history.picture

import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.aos.data.mapper.transferOrder
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.letspl.oceankeeper.util.ImgFileMaker
import com.letspl.oceankeeper.util.RotateTransform
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class InsertPictureViewModel @Inject constructor(
         @ApplicationContext private val context: Context,
    ): BaseViewModel() {

    private var _onClickedSaveWriting = MutableEventFlow<Boolean>()
    val onClickedSaveWriting: EventFlow<Boolean> get() = _onClickedSaveWriting

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedAddPicture = MutableEventFlow<Boolean>()
    val onClickedAddPicture: EventFlow<Boolean> get() = _onClickedAddPicture

    // 사진 촬영 uri
    private var takeCaptureUri: Uri? = null
    private var imageBitmapList: MutableList<Bitmap> = mutableListOf()
    // 파이어베이스 받은 url 리스트
    private val pictureUrlList = mutableListOf<String>()
    // 몇번째 이미지 인지
    private var pictureNum = 0
    private val ai: ApplicationInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    private val ak: String = ai.metaData["accessKey"].toString()
    private val sak: String = ai.metaData["secretAccessKey"].toString()
    private val wsCredentials: BasicAWSCredentials = BasicAWSCredentials(ak, sak)
    private val s3Client: AmazonS3Client =
        AmazonS3Client(wsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
    private val transferUtility: TransferUtility = TransferUtility.builder()
        .s3Client(s3Client)
        .context(context)
        .build()


    // 작성 저장하기 버튼 클릭
    fun onClickedSaveWriting() {
        baseEvent(Event.ShowLoading)
        imageBitmapList.forEach {
            uploadImageFile(it)
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

    // 파이어베이스 이미지 파일 업로드
    fun uploadImageFile(bitmap: Bitmap?) {
//        transferUtility.upload()


//            val storage = Firebase.storage
//            val storageRef = storage.reference
//            val imageRef = storageRef.child("dev/users/${getUserEmail()}/profile.jpg")
//            val baos = ByteArrayOutputStream()
//            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            val data = baos.toByteArray()
//
//            val uploadTask = imageRef.putBytes(data)
//            uploadTask.addOnFailureListener {
//                baseEvent(Event.HideLoading)
//                baseEvent(Event.ShowToast("프로필 변경이 실패하였습니다."))
//            }.addOnSuccessListener {
//                // 다운로드 링크 가져오기
//                it.storage.downloadUrl.addOnSuccessListener {uri ->
//                    // 성공
//                    // 사진 url finish 하면서 전달
//                    pictureUrlList.add(uri.toString())
//                }.addOnFailureListener {
//                    // 실패
//                    baseEvent(Event.HideLoading)
//                    baseEvent(Event.ShowToast("프로필 변경이 실패하였습니다."))
//                }
//        }
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
    fun setImageBitmap(bitmap: Bitmap?) {
        bitmap?.let { imageBitmapList.add(cropBitmapToSquare(it)) }
    }

    fun cropBitmapToSquare(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    fun getPictureNum(): Int {
        return pictureNum
    }

    // 임시 촬영 파일 불러오기
    fun getImageBitmap(num: Int): Bitmap {
        return imageBitmapList[num - 1]
    }

}