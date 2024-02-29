package app.ss.languages

import app.ss.languages.state.LanguageModel
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.libraries.circuit.navigation.LanguagesScreen
import ss.prefs.api.test.FakeSSPrefs
import ss.workers.api.test.FakeWorkScheduler

/** Tests for [LanguagesPresenter]. */
class LanguagesPresenterTest {

  private val languagesFlow = MutableSharedFlow<Result<List<Language>>>()
  private val fakeNavigator = FakeNavigator(LanguagesScreen)
  private val fakeRepository = FakeQuarterliesRepository(languagesFlow)
  private val fakeSSPrefs = FakeSSPrefs(MutableStateFlow("en"))
  private val fakeWorkScheduler = FakeWorkScheduler()

  private val underTest =
      LanguagesPresenter(
          navigator = fakeNavigator,
          repository = fakeRepository,
          ssPrefs = fakeSSPrefs,
          workScheduler = fakeWorkScheduler,
          dispatcherProvider = TestDispatcherProvider(),
      )

  @Test
  fun `present - emit models with properly formatted native language name`() = runTest {
    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      languagesFlow.emit(
          Result.success(
              listOf(
                  Language("en", "English", "English"),
                  Language("es", "Spanish", "Español"),
                  Language("fr", "French", "Français"),
              ),
          ),
      )

      val models = (awaitItem() as State.Languages).models
      models.toList() shouldBeEqualTo
          listOf(
              LanguageModel("en", "English", "English", true),
              LanguageModel("es", "Spanish", "Español", false),
              LanguageModel("fr", "French", "Français", false),
          )

      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `present - event - Search`() = runTest {
    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      languagesFlow.emit(Result.success(emptyList()))

      val state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Search("eng"))

      awaitItem() as State.Languages

      fakeRepository.setQuery shouldBeEqualTo "eng"

      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `present - event - Search - trim query`() = runTest {
    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      languagesFlow.emit(Result.success(emptyList()))

      val state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Search("   hello   "))

      awaitItem() as State.Languages

      fakeRepository.setQuery shouldBeEqualTo "hello"

      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `present - event - Select`() = runTest {
    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      languagesFlow.emit(Result.success(emptyList()))

      val state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Select(LanguageModel("es", "Spanish", "Español", false)))

      fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"
      fakeSSPrefs.setLastQuarterlyIndex shouldBeEqualTo null
      fakeWorkScheduler.preFetchImagesLanguage shouldBeEqualTo "es"

      ensureAllEventsConsumed()
    }
  }
}

private class FakeQuarterliesRepository(private val languagesFlow: Flow<Result<List<Language>>>) :
    QuarterliesRepository {

  var setQuery: String? = null
    private set

  override fun getLanguages(query: String?): Flow<Result<List<Language>>> {
    setQuery = query
    return languagesFlow
  }
}
