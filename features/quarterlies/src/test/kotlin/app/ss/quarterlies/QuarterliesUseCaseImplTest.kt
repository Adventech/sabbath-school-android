package app.ss.quarterlies

import app.ss.models.SSQuarterly
import app.ss.quarterlies.model.GroupedQuarterlies
import app.ss.quarterlies.model.spec
import kotlinx.collections.immutable.persistentListOf
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class QuarterliesUseCaseImplTest {

    private val underTest = QuarterliesUseCaseImpl()

    @Test
    fun `group ~ failure result ~ empty`() {
        underTest.group(Result.failure(Throwable())) shouldBeEqualTo GroupedQuarterlies.Empty
    }

    @Test
    fun `group ~ no groups ~ empty`() {
        val result = Result.success(emptyList<SSQuarterly>())

        underTest.group(result) shouldBeEqualTo GroupedQuarterlies.Empty
    }

    @Test
    fun `group ~ single group ~ list`() {
        val quarterly = SSQuarterly(id = "ID", quarterly_name = "Q1")
        val result = Result.success(listOf(quarterly))

        underTest.group(result) shouldBeEqualTo GroupedQuarterlies.TypeList(
            persistentListOf(quarterly.spec())
        )
    }

    @Test
    fun `group ~ multiple groups ~ grouped`() {
        val quarterly = SSQuarterly(id = "ID", quarterly_name = "Q1")
        val quarterly2 = SSQuarterly(id = "ID2", quarterly_name = "Q2")
        val result = Result.success(listOf(quarterly, quarterly2))

        underTest.group(result) shouldBeEqualTo GroupedQuarterlies.TypeList(
            persistentListOf(quarterly.spec(), quarterly2.spec())
        )
    }
}
