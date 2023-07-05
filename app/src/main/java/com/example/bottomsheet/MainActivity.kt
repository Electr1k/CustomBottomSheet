package com.example.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bottomsheet.ui.theme.BottomSheetTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope as rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomSheetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BottomSheetLayout()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetLayout() {
    val isHigh = remember { mutableStateOf(true) } // Открыть/закрыть bottom sheet
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = { SheetContent(sheetState, isHigh, scope) },
        sheetShape = if (sheetState.isExpanded && sheetState.targetValue==sheetState.currentValue && sheetState.progress.fraction==1f) RoundedCornerShape(0) else RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetPeekHeight = if (isHigh.value) 0.dp else 210.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green)
                .clickable(
                    // отключаем анимацию нажатия (кринж)
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isHigh.value = true
                    scope.launch { sheetState.collapse() }
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                scope.launch {
                    isHigh.value = !isHigh.value
                }
            }) {
                Text(text = "Bottom sheet fraction: ${sheetState.progress.fraction}")
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun SheetContent(sheetState: BottomSheetState, isHigh: MutableState<Boolean>, scope: CoroutineScope){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        val images = listOf(
            R.drawable.pager_1,
            R.drawable.pager_3,
            R.drawable.pager_2,
        )
        val pagerState = rememberPagerState(pageCount = images.size, initialPage = 0)
        val offsetForPager =
            // Задает смещение в dp от верха sheetBottom для того, чтобы скрыть pager при открытии
            if (sheetState.progress.fraction == 1f) {
                if (sheetState.isCollapsed) {
                    if (sheetState.currentValue == sheetState.targetValue) (-200).dp
                    else 0.dp
                } else {
                    if (sheetState.currentValue == sheetState.targetValue) 0.dp else (-200).dp
                }
            } else {
                if (sheetState.isCollapsed) ((-200) * (1f - sheetState.progress.fraction)).dp
                else ((-200) * (sheetState.progress.fraction)).dp
            }

        println("offsetForPager: $offsetForPager")
        Box{
            HorizontalPager(
                state = pagerState,
                dragEnabled = true,
                modifier = Modifier.offset(y = offsetForPager)
            ) { page ->
                Image(
                    painter = painterResource(id = images[page]),
                    contentDescription = null,
                    modifier = Modifier
                        .height(210.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.height(200.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                    horizontalArrangement = Arrangement.End){
                    Icon(painter = painterResource(id = R.drawable.camera_icon), contentDescription = null, tint = White)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = images.size.toString(), fontSize = 18.sp, color = White, fontWeight = W500)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Spacer(modifier = Modifier.width(7.dp))
                    for (i in images.indices) {
                        LinearIndicator(
                            modifier = Modifier.weight(1f),
                            indexIndicator = i,
                            pagerState = pagerState,
                            sheetState = sheetState)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        Card(
            elevation = 0.dp,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            modifier = Modifier.offset(y = offsetForPager-10.dp),
            backgroundColor = White
        ) {
            Column{
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)) {
                    Spacer(modifier = Modifier
                        .height(3.dp)
                        .width(30.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Gray))
                }
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Аудитория Г-337",
                        fontSize = 28.sp,
                        fontWeight = W500,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clickable {
                                isHigh.value = true
                                scope.launch { sheetState.collapse() }
                            }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "В этой аудитории вы можете поработать за компьюетрами.",
                    fontSize = 18.sp,
                    fontWeight = W500,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "До закрытия 1 час",
                    fontSize = 18.sp,
                    fontWeight = W500,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFB75301)
                )
                Spacer(modifier = Modifier.height(20.dp))
                repeat(30) {
                    Text(
                        text = "Тут много текста",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

            }
        }
    }
}



@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
@Composable
fun LinearIndicator(modifier: Modifier, indexIndicator:Int, pagerState: PagerState, sheetState: BottomSheetState){
    var progress by remember{
        mutableStateOf(0.00f)
    }
    val animatedProgress by animateFloatAsState(targetValue = if (indexIndicator == pagerState.currentPage) progress else 0.00f, animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec)
    if (indexIndicator == pagerState.currentPage&&sheetState.progress.fraction==1f&&sheetState.isExpanded && sheetState.targetValue==sheetState.currentValue){
        LaunchedEffect(key1 = Unit){
            progress = 0.00f
            while (progress < 1f){
                if (pagerState.currentPage != indexIndicator){
                    progress = 0.00f
                    break
                }
                progress += 0.02f
                delay(50)
            }
            if (indexIndicator == pagerState.currentPage ){
                pagerState.animateScrollToPage((pagerState.currentPage + 1).rem(pagerState.pageCount))
                progress = 0.00f
            }
        }
    }
    LinearProgressIndicator(
        modifier = modifier
            .padding(bottom = 10.dp, start = 5.dp)
            .clip(RoundedCornerShape(2.dp)),
        backgroundColor = if (indexIndicator < pagerState.currentPage ) White else Color.LightGray,
        color = White,
        progress = animatedProgress
    )
}

@Preview
@Composable
fun Preview(){
    BottomSheetLayout()
}