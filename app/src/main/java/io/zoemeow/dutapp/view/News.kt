package io.zoemeow.dutapp.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.zoemeow.dutapp.R
import io.zoemeow.dutapp.model.enums.ProcessResult
import io.zoemeow.dutapp.pagerTabIndicatorOffset
import io.zoemeow.dutapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun News(mainViewModel: MainViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        val tabTitles = listOf(
            stringResource(id = R.string.navnews_navtab_newsglobal),
            stringResource(id = R.string.navnews_navtab_newssubject)
        )
        val pagerState = rememberPagerState(initialPage = 0)
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = { },
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        // TODO: Search in news global and news subject here!
                    },
                    content = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_search_24),
                            contentDescription = "Search",
                        )
                    }
                )
            }
        ) { paddingValue ->
            Column(modifier = Modifier.padding(paddingValue)) {
                TabRow(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                    selectedTabIndex = pagerState.currentPage,
                    indicator = {
                            tabPositions ->
                        // This is a temporary fix for require material2 instead of material3.
                        // https://github.com/google/accompanist/issues/1076
                        // Waiting for a release fix for this library.
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, text ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    text = text,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                    }
                }
                val isLoadingGlobal = (
                        try { mainViewModel.variableData.get<ProcessResult>("NewsGlobal")!!.value.value == ProcessResult.Running }
                        catch (_: Exception) { false }
                        )
                val isLoadingSubject = (
                        try { mainViewModel.variableData.get<ProcessResult>("NewsSubject")!!.value.value == ProcessResult.Running }
                        catch (_: Exception) { false }
                        )

                HorizontalPager(count = tabTitles.size, state = pagerState) { index ->
                    when (index) {
                        0 -> NewsGlobalViewHost(
                            newsDetailsClickedData = mainViewModel.newsDetailsClickedData,
                            isLoading = isLoadingGlobal,
                            data = mainViewModel.newsCacheData,
                            getDataRequested = { forceValue ->
                                if (!isLoadingGlobal)
                                    mainViewModel.getNewsGlobal(force = forceValue)
                            },
                        )
                        1 -> NewsSubjectViewHost(
                            newsDetailsClickedData = mainViewModel.newsDetailsClickedData,
                            isLoading = isLoadingSubject,
                            data = mainViewModel.newsCacheData,
                            getDataRequested = { forceValue ->
                                if (!isLoadingSubject)
                                    mainViewModel.getNewsSubject(force = forceValue)
                            },
                        )
                    }
                }
            }
        }

    }
}

@SuppressLint("SimpleDateFormat")
fun getDateString(date: Long, dateFormat: String, gmt: String = "UTC"): String {
    // "dd/MM/yyyy"
    // "dd/MM/yyyy HH:mm"
    val simpleDateFormat = SimpleDateFormat(dateFormat)
    simpleDateFormat.timeZone = TimeZone.getTimeZone(gmt)
    return simpleDateFormat.format(Date(date))
}
