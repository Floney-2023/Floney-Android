package com.aos.floney.view.history.picture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.home.ImageUrls
import com.aos.usecase.subscribe.SubscribePresignedUrlUseCase
import com.aos.floney.util.ImgFileMaker
import com.aos.floney.util.ImgFileMaker.createBitmapFromUri
import com.aos.model.settlement.BookUsers
import com.aos.model.subscribe.PictureItem
import com.aos.model.subscribe.SelectablePicture
import com.aos.model.subscribe.UiPictureSelectModel
import com.letspl.oceankeeper.util.RotateTransform
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class InsertPictureViewModel @Inject constructor(
    @ApplicationContext private val context: Context
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

    private var _sortPictures = MutableLiveData<UiPictureSelectModel>()
    val sortPictures: LiveData<UiPictureSelectModel> get() = _sortPictures

    // 사진 촬영 uri
    private var takeCaptureUri: Uri? = null

    // 로컬 이미지 리스트 (File 형태)
    private var localImageList: MutableList<File> = mutableListOf()

    // S3 받은 url 리스트 (String 형태 - 서버에 보낼 값)
    private var cloudImageList = mutableListOf<ImageUrls>()

    // 몇번째 이미지 인지
    private var isModify = false

    // 삭제 모드면 true, 보기 모드면 false
    val isDeleteMode = MutableLiveData<Boolean>(false)

    // 모든 토글이 선택된 여부 확인
    val isAllSelected = MediatorLiveData<Boolean>().apply {
        addSource(sortPictures) { model ->
            value = model.selectablePictures.all { it.isSelected }
        }
    }

    fun toggleSelectAll(isSelectAll: Boolean) {
        val current = _sortPictures.value ?: return
        val updated = current.selectablePictures.map {
            it.copy(isSelected = isSelectAll)
        }
        _sortPictures.postValue(current.copy(selectablePictures = updated))
    }

    fun deleteSelectedPictures() {
        val selected = _sortPictures.value?.selectablePictures?.filter { it.isSelected } ?: return

        // 클라우드/로컬 나누기
        val selectedCloud = selected.mapNotNull { it.picture as? PictureItem.CloudImage }
        val selectedLocal = selected.mapNotNull { it.picture as? PictureItem.LocalImage }

        cloudImageList.removeAll { cloud -> selectedCloud.any { it.imageUrls == cloud } }
        localImageList.removeAll { local -> selectedLocal.any { it.file == local } }

        // 삭제 완료 후 삭제 모드 OFF
        isDeleteMode.value = false
        baseEvent(Event.ShowSuccessToast("사진 삭제가 완료되었습니다."))

        updateImageList()
    }

    // 클라우드 이미지 리스트 세팅 (내용 수정 시)
    fun initPhotoList(cloudUrls: List<ImageUrls>?, localUrls: ArrayList<File>?) {
        cloudUrls?.let {  cloudImageList = it.toMutableList() }
        localUrls?.let {  localImageList = it.toMutableList() }
        updateImageList()
    }

    // 작성 저장하기 버튼 클릭
    fun onClickedSaveWriting() {
        viewModelScope.launch {

            Timber.e("localImageList $localImageList")
            Timber.e("cloudImageList $cloudImageList")

            _onSuccessImageUpload.emit(cloudImageList)
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

    fun onClickedBottomButton() {
        // 삭제 모드면 이미지 삭제
        if (isDeleteMode.value!!){
            deleteSelectedPictures()
        } else {
            // 삭제 모드가 아니면 이미지 추가
            onClickedAddPicture()
        }
    }

    fun onSetDeleteMode(delete: Boolean) {
        isDeleteMode.value = delete
        _sortPictures.value = _sortPictures.value?.copy(
            selectablePictures = _sortPictures.value?.selectablePictures?.map {
                it.copy(isDeleteMode = delete)
            } ?: emptyList()
        )
    }

    fun setIsModify(checkModify: Boolean) {
        isModify = checkModify
    }

    fun getIsModify() : Boolean {
        return isModify
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
            try {
                val bitmap = if (uri.toString().startsWith("content://media/picker")) {
                    // PhotoPicker URI인 경우 직접 회전 처리된 비트맵 생성
                    RotateTransform.createRotatedBitmapFromUri(context, uri)
                } else {
                    // 기존 방식으로 처리
                    val path = ImgFileMaker.getFullPathFromUri(context, uri)
                    if (path != null) {
                        val angle = RotateTransform.getRotationAngle(path)
                        RotateTransform.rotateImage(
                            context,
                            BitmapFactory.decodeFile(path),
                            angle.toFloat(),
                            uri
                        )
                    } else {
                        // 경로를 얻지 못한 경우 URI에서 직접 비트맵 생성
                        RotateTransform.createRotatedBitmapFromUri(context, uri)
                    }
                }

                if (bitmap != null) {
                    // 3:4 비율로 크롭
                    val croppedBitmap = cropToAspectRatio(bitmap, 3f, 4f)

                    val imageFile =
                        File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")

                    try {
                        val fos = FileOutputStream(imageFile)
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.flush()
                        fos.close()
                        localImageList.add(imageFile)
                        isModify = true
                        viewModelScope.launch {
                            updateImageList()
                        }
                        croppedBitmap
                    } catch (e: Exception) {
                        Timber.e(e, "Error saving bitmap to file")
                        baseEvent(Event.ShowToast("이미지 파일 생성에 실패하였습니다."))
                        null
                    }
                } else {
                    baseEvent(Event.ShowToast("이미지 파일 생성에 실패하였습니다."))
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in createBitmapFile")
                baseEvent(Event.ShowToast("이미지 파일 생성에 실패하였습니다."))
                null
            }
        } else {
            baseEvent(Event.ShowToast("이미지 파일 설정에 실패하였습니다."))
            null
        }
    }

    // 이미지를 지정된 비율로 중앙 크롭하는 함수
    private fun cropToAspectRatio(bitmap: Bitmap, targetWidth: Float, targetHeight: Float): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        val targetRatio = targetWidth / targetHeight
        val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()

        // 이미 3:4 비율에 가까우면 그대로 반환
        if (Math.abs(originalRatio - targetRatio) < 0.01f) {
            return bitmap
        }

        val newWidth: Int
        val newHeight: Int

        if (originalRatio > targetRatio) {
            // 원본이 더 넓은 경우 - 높이를 기준으로 크롭
            newHeight = originalHeight
            newWidth = (originalHeight * targetRatio).toInt()
        } else {
            // 원본이 더 높은 경우 - 너비를 기준으로 크롭
            newWidth = originalWidth
            newHeight = (originalWidth / targetRatio).toInt()
        }

        val x = (originalWidth - newWidth) / 2
        val y = (originalHeight - newHeight) / 2

        return Bitmap.createBitmap(bitmap, x, y, newWidth, newHeight)
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
            null
        }
    }

    // 선택한 이미지 설정
    fun settingSelectPicture(selectPicture: SelectablePicture)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = _sortPictures.value?.selectablePictures?.map { checkPicture ->
                if (checkPicture.picture == selectPicture.picture) {
                    checkPicture.copy(isSelected = !selectPicture.isSelected) // 선택된 멤버의 isCheck를 true로 설정
                } else {
                    checkPicture
                }
            }
            _sortPictures.postValue(_sortPictures.value?.copy(selectablePictures = updatedList!!))
        }
    }

    fun deletePictureFile(imageUrls: ImageUrls) {
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
            updateImageList()
        }
    }

    fun updateImageList() {
        viewModelScope.launch {
            // 클라우드 리스트를 PictureItem.CloudImage로 변환 + 로컬 리스트를 PictureItem.LocalImage로 변환
            val sortedList: List<PictureItem> =  cloudImageList.map { PictureItem.CloudImage(ImageUrls(it.id, it.url)) } +  // S3 URL 리스트 변환
                    localImageList.map { PictureItem.LocalImage(it) }        // 로컬 파일 리스트 변환

            // isSelected는 기본값 false로 초기화
            val selectableList = sortedList.map {
                SelectablePicture(it)
            }
            _sortPictures.postValue(UiPictureSelectModel(selectableList))
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

    fun getCloudPictureList(): List<ImageUrls> {
        return cloudImageList
    }

    fun getLocalPictureList() : List<File> {
        return localImageList
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
