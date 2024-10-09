package app.ss.media.playback.ui.video

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.ss.lessons.data.repository.media.MediaRepository
import app.ss.models.media.AudioFile
import app.ss.models.media.SSAudio
import app.ss.models.media.SSVideo
import app.ss.models.media.SSVideosInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import ss.misc.SSConstants

private const val LESSON_INDEX = "2022-01-01"
private val fakeVideo = SSVideo("Artist", "id", "src", "target", "targetIndex", "thumbnail", "title")

@RunWith(AndroidJUnit4::class)
class VideoListViewModelTest {

    private val videoFlow = MutableStateFlow<List<SSVideosInfo>>(emptyList())
    private val repository = FakeMediaRepository(videoFlow)

    private val underTest = VideoListViewModel(
        repository = repository,
        savedStateHandle = SavedStateHandle(
            mapOf(SSConstants.SS_LESSON_INDEX_EXTRA to LESSON_INDEX),
        ),
    )

    @Test
    fun `videoListFlow - Vertical state`() = runTest {
        val featuredVideo = fakeVideo.copy(targetIndex = LESSON_INDEX)
        underTest.videoListFlow.test {
            awaitItem() shouldBeEqualTo VideoListData.Horizontal(
                data = emptyList(),
                target = LESSON_INDEX
            )

            videoFlow.emit(
                listOf(
                    SSVideosInfo(
                        id = "id",
                        artist = "artist",
                        clips = listOf(fakeVideo, featuredVideo),
                        lessonIndex = LESSON_INDEX,
                    )
                )
            )

            awaitItem() shouldBeEqualTo VideoListData.Vertical(
                featured = featuredVideo,
                clips = listOf(fakeVideo),
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `videoListFlow - Horizontal state`() = runTest {
        val videos = listOf(fakeVideo, fakeVideo.copy(id = "id2"))
        val data = listOf(
            SSVideosInfo(
                id = "id",
                artist = "artist",
                clips = videos,
                lessonIndex = LESSON_INDEX,
            ),
            SSVideosInfo(
                id = "id2",
                artist = "artist2",
                clips = videos,
                lessonIndex = LESSON_INDEX,
            )
        )
        underTest.videoListFlow.test {
            awaitItem() shouldBeEqualTo VideoListData.Horizontal(
                data = emptyList(),
                target = LESSON_INDEX
            )

            videoFlow.emit(
                listOf(
                    SSVideosInfo(
                        id = "id",
                        artist = "artist",
                        clips = videos,
                        lessonIndex = LESSON_INDEX,
                    ),
                    SSVideosInfo(
                        id = "id2",
                        artist = "artist2",
                        clips = videos,
                        lessonIndex = LESSON_INDEX,
                    )
                )
            )

            awaitItem() shouldBeEqualTo VideoListData.Horizontal(
                data = data,
                target = LESSON_INDEX
            )

            ensureAllEventsConsumed()
        }
    }
}

private class FakeMediaRepository(
    private val videoFlow: Flow<List<SSVideosInfo>>
) : MediaRepository {
    override fun getAudio(lessonIndex: String): Flow<List<SSAudio>> {
        return emptyFlow()
    }

    override suspend fun findAudioFile(id: String): AudioFile? {
        return null
    }

    override suspend fun getPlayList(lessonIndex: String): List<AudioFile> {
        return emptyList()
    }

    override fun getVideo(lessonIndex: String): Flow<List<SSVideosInfo>> {
        return videoFlow
    }

}
