package org.dhis2.usescases.programEventDetail

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS

class ProgramEventDetailRepositoryTest {

    private lateinit var repository: ProgramEventDetailRepository

    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val programUid = "programUid"

    @Before
    fun setUp() {
        repository = ProgramEventDetailRepositoryImpl(programUid, d2)
    }

    @Test
    fun `Should get the ProgramEventModel by using the event uid`() {
        val event = dummyEvent()

        whenever(d2.eventModule().events().byUid()) doReturn mock()
        whenever(d2.eventModule().events().byUid().eq("eventUid")) doReturn mock()
        whenever(
            d2.eventModule().events().byUid().eq("eventUid").withTrackedEntityDataValues()
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byUid().eq("eventUid").withTrackedEntityDataValues().one()
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byUid().eq("eventUid").withTrackedEntityDataValues().one()
                .get()
        ) doReturn Single.just(event)
        mockTransformToProgramEventModel()

        val testObserver = repository.getInfoForEvent("eventUid").test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.orgUnitName() == "OrgUnitName" && it.eventState() == State.TO_UPDATE
        }
    }

    @Test
    fun `Should return the feature type of a programStage`() {
        whenever(d2.programModule().programStages().byProgramUid().eq(programUid)) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().get()
        ) doReturn Single.just(
            ProgramStage.builder().uid("uid").featureType(FeatureType.POINT).build()
        )

        val testObserver = repository.featureType().test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == FeatureType.POINT
        }
    }

    @Test
    fun `Should return FeatureType NONE if a programStage does not have featureType`() {
        whenever(d2.programModule().programStages().byProgramUid().eq(programUid)) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().get()
        ) doReturn Single.just(ProgramStage.builder().uid("uid").build())

        val testObserver = repository.featureType().test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == FeatureType.NONE
        }
    }

    @Test
    fun `Should return if user has programStages that are assigned to the user`() {
        whenever(d2.programModule().programStages().byProgramUid().eq(programUid)) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid)
                .byEnableUserAssignment()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid)
                .byEnableUserAssignment().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid)
                .byEnableUserAssignment().isTrue.blockingIsEmpty()
        ) doReturn false

        val hasAssignment = repository.hasAssignment()

        assert(hasAssignment)
    }

    @Test
    fun `Should return the program`() {
        val program = Program.builder().uid(programUid).build()

        whenever(d2.programModule().programs().uid(programUid)) doReturn mock()
        whenever(d2.programModule().programs().uid(programUid).blockingGet()) doReturn program

        val testObserver = repository.program().test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.uid() == programUid
        }
    }

    @Test
    fun `Should return true if the user has Access Data Write permission`() {
        mockProgramAccess()
        mockProgramStage()
        whenever(
            d2.programModule().programs().uid(programUid).blockingGet().access().data().write()
        ) doReturn true
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
                .access().data().write()
        ) doReturn true

        val hasWritePermission = repository.accessDataWrite

        assert(hasWritePermission)
    }

    @Test
    fun `Should return false if the user has Write permission but programStage is null`() {
        mockProgramAccess()
        mockNullProgramStage()
        whenever(
            d2.programModule().programs().uid(programUid).blockingGet().access().data().write()
        ) doReturn true

        val hasWritePermission = repository.accessDataWrite

        assert(!hasWritePermission)
    }

    private fun mockTransformToProgramEventModel() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()
    }

    private fun mockOrgUnitName() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid")
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet()
                .displayName()
        ) doReturn "OrgUnitName"
    }

    private fun mockProgramStageDataElements() {
        whenever(d2.programModule().programStageDataElements().byProgramStage()) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements().byProgramStage().eq("programStage")
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage").blockingGet()
        ) doReturn emptyList()
    }

    private fun mockProgram() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()
        ) doReturn dummyProgramWithExpiryInfo()
    }

    private fun mockCategoryOptionCombo() {
        whenever(d2.categoryModule().categoryOptionCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().uid("attrComboUid")) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().uid("attrComboUid").blockingGet()
        ) doReturn dummyCategoryOptionCombo()
    }

    private fun mockProgramAccess() {
        whenever(d2.programModule().programs().uid(programUid)) doReturn mock()
        whenever(d2.programModule().programs().uid(programUid).blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid(programUid).blockingGet().access()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid(programUid).blockingGet().access().data()
        ) doReturn mock()
    }

    private fun mockProgramStage() {
        whenever(d2.programModule().programStages().byProgramUid().eq(programUid)) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
        ) doReturn ProgramStage.builder().uid("stageUid").build()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
                .access()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
                .access().data()
        ) doReturn mock()
    }

    private fun mockNullProgramStage() {
        whenever(d2.programModule().programStages().byProgramUid().eq(programUid)) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet()
        ) doReturn null
    }

    private fun dummyEvent() =
        Event.builder()
            .uid("eventUid")
            .organisationUnit("orgUnitUid")
            .eventDate(Date())
            .program("programUid")
            .programStage("programStage")
            .attributeOptionCombo("attrComboUid")
            .status(EventStatus.ACTIVE)
            .build()

    private fun dummyProgramWithExpiryInfo() =
        Program.builder()
            .uid("programUid")
            .completeEventsExpiryDays(0)
            .expiryDays(0)
            .build()

    private fun dummyCategoryOptionCombo() =
        CategoryOptionCombo.builder().uid("attrComboUid").displayName("default").build()
}
