package app.ss.languages

import app.ss.languages.state.LanguageModel
import app.ss.lessons.data.repository.quarterly.QuarterliesRepository
import app.ss.models.Language
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
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

  private val fakeNavigator = FakeNavigator(LanguagesScreen)
  private val fakeRepository = FakeQuarterliesRepository()
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
    fakeRepository.getLanguagesDelegate = {
      Result.success(
          listOf(
              Language("en", "English", "English"),
              Language("es", "Spanish", "Español"),
              Language("fr", "French", "Français"),
          ),
      )
    }

    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

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
    val query = "english"
    fakeRepository.getLanguagesDelegate = {
      if (it == query) {
        Result.success(listOf(Language("en", "English", "English")))
      } else {
        Result.success(emptyList())
      }
    }

    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      var state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Search(query))

      awaitItem()

      state = awaitItem() as State.Languages

      state.models.first().code shouldBeEqualTo "en"

      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `present - event - Search - trim query`() = runTest {
    val query = "hello"
    fakeRepository.getLanguagesDelegate = {
      if (it == query) {
        Result.success(listOf(Language("hello", "English", "English")))
      } else {
        Result.success(emptyList())
      }
    }

    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      var state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Search("   hello   "))

      awaitItem()

      state = awaitItem() as State.Languages

      state.models.first().code shouldBeEqualTo "hello"

      ensureAllEventsConsumed()
    }
  }

  @Test
  fun `present - event - Select`() = runTest {
    fakeRepository.getLanguagesDelegate = { Result.success(emptyList()) }

    underTest.test {
      awaitItem() shouldBeInstanceOf State.Loading::class.java

      val state = awaitItem() as State.Languages

      state.eventSink(LanguagesEvent.Select(LanguageModel("es", "Spanish", "Español", false)))

      fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"
      fakeSSPrefs.setLastQuarterlyIndex shouldBeEqualTo null
      fakeWorkScheduler.preFetchImagesLanguage shouldBeEqualTo "es"

      ensureAllEventsConsumed()
    }
  }
}

private class FakeQuarterliesRepository : QuarterliesRepository {

  var getLanguagesDelegate: (String?) -> Result<List<Language>> = { error("Not implemented") }

  override suspend fun getLanguages(query: String?): Result<List<Language>> {
    return getLanguagesDelegate(query)
  }
}
