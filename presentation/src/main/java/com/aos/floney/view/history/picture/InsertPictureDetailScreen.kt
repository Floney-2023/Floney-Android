package com.aos.floney.view.history.picture

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.aos.floney.R
import com.aos.model.home.ImageUrls
import java.io.File

private val Grayscale1 = Color(0xFF0D0D0D)
private val Grayscale2 = Color(0xFF262626)
private val Grayscale9 = Color(0xFFD9D9D9)
private val Grayscale10 = Color(0xFFF1F1F1)

@Composable
fun InsertPictureDetailScreen(
    images: List<ImageUrls>,
    startIndex: Int,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = startIndex) { images.size }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            ) { page ->
                ImagePage(imageUrls = images[page])
            }

            if (images.size > 1) {
                DotsIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        TopBar(
            onBack = onBack,
            onDeleteClick = { showDeleteDialog = true },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete(pagerState.currentPage)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
    val pretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))
    val pretendardRegular = FontFamily(Font(R.font.floney_pretendard_regular))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_back_black),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }

        Text(
            text = stringResource(id = R.string.picture_detail_title),
            fontFamily = pretendardSemiBold,
            fontSize = 16.sp,
            color = Grayscale1
        )

        Text(
            text = stringResource(id = R.string.picture_delete),
            fontFamily = pretendardRegular,
            fontSize = 14.sp,
            color = Grayscale2,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onDeleteClick() }
                .padding(horizontal = 20.dp, vertical = 19.dp)
        )
    }
}

@Composable
private fun ImagePage(imageUrls: ImageUrls) {
    val model = if (imageUrls.id != -1) imageUrls.url else File(imageUrls.url)
    SubcomposeAsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> ShimmerBox()
            else -> SubcomposeAsyncImageContent()
        }
    }
}

@Composable
private fun ShimmerBox() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFFE8E8E8), Color(0xFFF5F5F5), Color(0xFFE8E8E8)),
        start = Offset(offset - 1000f, 0f),
        end = Offset(offset, 0f)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    )
}

@Composable
private fun DotsIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { page ->
            val selected = pagerState.currentPage == page
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.background(Grayscale2)
                        else Modifier.background(Grayscale9)
                    )
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val pretendardBold = FontFamily(Font(R.font.floney_pretendard_bold))
    val pretendardRegular = FontFamily(Font(R.font.floney_pretendard_regular))
    val pretendardSemiBold = FontFamily(Font(R.font.pretendard_semibold))

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .size(width = 300.dp, height = 148.dp)
                .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(id = R.string.dialog_delete_photo_title),
                fontFamily = pretendardBold,
                fontSize = 16.sp,
                color = Grayscale1
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(id = R.string.dialog_delete_photo_message),
                fontFamily = pretendardRegular,
                fontSize = 13.sp,
                color = Grayscale1
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Grayscale10)
            )

            Row(modifier = Modifier.height(48.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_yes),
                        fontFamily = pretendardSemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFFDE0009)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(1.dp, 48.dp)
                        .background(Grayscale10)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_no),
                        fontFamily = pretendardSemiBold,
                        fontSize = 16.sp,
                        color = Grayscale2
                    )
                }
            }
        }
    }
}
