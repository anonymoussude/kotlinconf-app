package org.jetbrains.kotlinconf.android.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import coil.compose.*
import com.jetbrains.kotlinconf.R
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.android.*
import org.jetbrains.kotlinconf.android.theme.*
import org.jetbrains.kotlinconf.android.ui.components.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SessionDetailed(
    id: String,
    time: String,
    title: String,
    description: String,
    location: String,
    isFavorite: Boolean,
    vote: Score?,
    isFinished: Boolean,
    speakers: List<Speaker>,
    isLightning: Boolean,
    isCodeLab: Boolean,
    isAWS: Boolean,
    controller: AppController
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Expanded)
    )
    var showFeedbackBlock by remember { mutableStateOf(false) }

    val session = @Composable {
        SessionContent(
            time = time,
            title = title,
            description = description,
            location = location,
            isFavorite = isFavorite,
            onFavoriteClick = { controller.toggleFavorite(id) },
            speakers = speakers,
            isLightning = isLightning,
            isCodeLab = isCodeLab,
            isAWS = isAWS,
            onBackClick = { controller.back() },
            onSpeakerClick = { speakerId -> controller.showSpeaker(speakerId) }
        )
    }

    if (isFinished) {
        BottomSheetScaffold(
            sheetContent = {
                Column {
                    Spacer(Modifier.height(4.dp))
                    SheetBar()
                    VoteAndFeedback(
                        vote = vote,
                        showFeedbackBlock = showFeedbackBlock,
                        onVote = {
                            controller.vote(id, it)
                            showFeedbackBlock = true
                        },
                        onSubmitFeedback = {
                            controller.sendFeedback(id, it)
                            showFeedbackBlock = false
                        }, onCloseFeedback = {
                            showFeedbackBlock = false
                        }
                    )

                    Spacer(Modifier.height(4.dp))
                }
            },
            sheetPeekHeight = 100.dp,
            sheetBackgroundColor = MaterialTheme.colors.whiteGrey,
            scaffoldState = scaffoldState
        ) {
            session()
        }
    } else {
        session()
    }
}

@Composable
private fun SessionContent(
    time: String,
    title: String,
    description: String,
    location: String,
    isFavorite: Boolean,
    speakers: List<Speaker>,
    isLightning: Boolean,
    isCodeLab: Boolean,
    isAWS: Boolean,
    onFavoriteClick: () -> Unit,
    onSpeakerClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        NavigationBar(
            title = "SESSION",
            isLeftVisible = true,
            onLeftClick = onBackClick,
            isRightVisible = false,
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .fillMaxHeight()
                .background(MaterialTheme.colors.whiteGrey)
        ) {
            SessionHead(time, title, isFavorite, onFavoriteClick, onSpeakerClick, speakers)
            SessionDetails(description, location, isLightning, isCodeLab, isAWS)
            MapBoxMap(
                uri = mapByLocation(location),
                cameraOptions = roomByLocation(location),
                modifier = Modifier
                    .height(400.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SessionHead(
    time: String,
    title: String,
    isFavorite: Boolean,
    favoriteClick: () -> Unit,
    speakerClick: (String) -> Unit,
    speakers: List<Speaker>
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.grey5Black)
    ) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp)) {
            Row {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = time.uppercase(),
                    style = MaterialTheme.typography.t2.copy(
                        color = MaterialTheme.colors.grey50Grey20
                    ),
                )
                Spacer(modifier = Modifier.weight(1f))

                FavoriteButton(isFavorite, favoriteClick)
            }
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.h2.copy(
                    color = MaterialTheme.colors.greyWhite
                )
            )

            speakers.forEach {
                Text(
                    it.name,
                    style = MaterialTheme.typography.t2.copy(
                        color = MaterialTheme.colors.greyGrey20
                    )
                )
            }
        }
        SpeakerPhotos(speakers, speakerClick)
        HDivider()
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, favoriteClick: () -> Unit) {
    IconButton(onClick = favoriteClick) {
        val icon = if (isFavorite) R.drawable.bookmark_active else R.drawable.bookmark
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "favorite",
            tint = if (isFavorite) orange else MaterialTheme.colors.greyGrey5
        )
    }
}

@Composable
private fun SpeakerPhotos(speakers: List<Speaker>, speakerClick: (String) -> Unit) {
    Row(Modifier.padding(top = 24.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)) {
        speakers.forEach { speaker ->
            AsyncImage(
                modifier = Modifier
                    .size(if (speakers.size < 3) 120.dp else 78.dp)
                    .padding(end = 8.dp)
                    .clickable { speakerClick(speaker.id) },
                model = speaker.photoUrl,
                contentDescription = speaker.name,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun SessionDetails(
    description: String,
    location: String,
    isLightning: Boolean,
    isCodeLab: Boolean,
    isAWS: Boolean,
) {
    Column(Modifier.background(MaterialTheme.colors.whiteGrey)) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()) {
            if (isLightning) {
                LightningTalk("Lightning Talk")
            } else if (isCodeLab) {
                CodeLab()
            } else if (isAWS) {
                AWSLab()
            }
        }

        Column(Modifier.padding(16.dp)) {
            Text(
                description,
                style = MaterialTheme.typography.t2.copy(
                    color = MaterialTheme.colors.greyGrey20
                )
            )
            LocationRow(location, Modifier.padding(top = 24.dp))
        }
    }
}
