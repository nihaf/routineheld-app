package de.routineheld.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.routineheld.app.data.local.AppDatabase
import de.routineheld.app.data.local.dao.ActivityDao
import de.routineheld.app.data.local.dao.RoutinePlanDao
import de.routineheld.app.data.local.dao.WeekPlanDao
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntryEntity
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var activityDao: ActivityDao
    private lateinit var planDao: RoutinePlanDao
    private lateinit var weekPlanDao: WeekPlanDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            // Allow main thread queries for testing simplicity
            .allowMainThreadQueries()
            .build()

        activityDao = db.activityDao()
        planDao = db.routinePlanDao()
        weekPlanDao = db.weekPlanDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // =========================================================================
    // ActivityDao Tests (5)
    // =========================================================================

    @Test
    fun activityDao_insertAndRetrieve() = runTest {
        val activity = ActivityEntity(
            name = "Zähne putzen",
            iconRef = "ic_brush_teeth"
        )
        val id = activityDao.insert(activity)
        assertTrue(id > 0)

        val retrieved = activityDao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Zähne putzen", retrieved!!.name)
        assertEquals("ic_brush_teeth", retrieved.iconRef)
    }

    @Test
    fun activityDao_update() = runTest {
        val id = activityDao.insert(
            ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up")
        )

        val original = activityDao.getById(id)!!
        activityDao.update(original.copy(name = "Aufstehen", color = 2))

        val updated = activityDao.getById(id)!!
        assertEquals("Aufstehen", updated.name)
        assertEquals(2, updated.color)
    }

    @Test
    fun activityDao_delete() = runTest {
        val id = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )
        activityDao.deleteById(id)

        val result = activityDao.getById(id)
        assertNull(result)
    }

    @Test
    fun activityDao_usageCount_returnsZero_whenUnused() = runTest {
        val id = activityDao.insert(
            ActivityEntity(name = "Unbenutzt", iconRef = "ic_unused")
        )

        val count = activityDao.getUsageCount(id)
        assertEquals(0, count)
    }

    @Test
    fun activityDao_usageCount_returnsCorrectCount_whenUsed() = runTest {
        val activityId = activityDao.insert(
            ActivityEntity(name = "Zähne putzen", iconRef = "ic_brush_teeth")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Morgenroutine", slotCount = 6)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = activityId, position = 0)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = activityId, position = 1)
        )

        val count = activityDao.getUsageCount(activityId)
        assertEquals(2, count)
    }

    // =========================================================================
    // RoutinePlanDao Tests (6)
    // =========================================================================

    @Test
    fun planDao_insertAndRetrieveAll() = runTest {
        planDao.insert(RoutinePlanEntity(name = "Plan A", slotCount = 4))
        planDao.insert(RoutinePlanEntity(name = "Plan B", slotCount = 6))

        val plans = planDao.getAll().first()
        assertEquals(2, plans.size)
    }

    @Test
    fun planDao_insertEntriesAndRetrieveWithRelation() = runTest {
        val actId1 = activityDao.insert(
            ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up")
        )
        val actId2 = activityDao.insert(
            ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Morgenroutine", slotCount = 4)
        )

        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId1, position = 0)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId2, position = 1)
        )

        val planWithEntries = planDao.getPlanWithEntriesOnce(planId)
        assertNotNull(planWithEntries)
        assertEquals("Morgenroutine", planWithEntries!!.plan.name)
        assertEquals(2, planWithEntries.entries.size)
    }

    @Test
    fun planDao_deleteAllEntries() = runTest {
        val actId = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Plan", slotCount = 4)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId, position = 0)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId, position = 1)
        )

        planDao.deleteAllEntries(planId)

        val planWithEntries = planDao.getPlanWithEntriesOnce(planId)
        assertNotNull(planWithEntries)
        assertTrue(planWithEntries!!.entries.isEmpty())
    }

    @Test
    fun planDao_updateEntryPosition() = runTest {
        val actId = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Plan", slotCount = 4)
        )
        val entryId = planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId, position = 0)
        )

        planDao.updateEntryPosition(entryId, 5)

        val entries = planDao.getEntriesForPlan(planId)
        assertEquals(1, entries.size)
        assertEquals(5, entries[0].position)
    }

    @Test
    fun planDao_cascadeDelete_planRemovesEntries() = runTest {
        val actId = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Plan", slotCount = 4)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId, position = 0)
        )

        planDao.deleteById(planId)

        // Plan is gone
        val plan = planDao.getPlanWithEntriesOnce(planId)
        assertNull(plan)

        // Activity still exists (only entries are cascaded, not the activity itself)
        val activity = activityDao.getById(actId)
        assertNotNull(activity)
    }

    @Test
    fun planDao_cascadeDelete_activityRemovesEntries() = runTest {
        val actId = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Plan", slotCount = 4)
        )
        planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = actId, position = 0)
        )

        activityDao.deleteById(actId)

        // Plan still exists but entries referencing the activity are gone
        val planWithEntries = planDao.getPlanWithEntriesOnce(planId)
        assertNotNull(planWithEntries)
        assertTrue(planWithEntries!!.entries.isEmpty())
    }

    // =========================================================================
    // WeekPlanDao Tests (5)
    // =========================================================================

    @Test
    fun weekPlanDao_insertAndRetrieve() = runTest {
        val id = weekPlanDao.insert(
            WeekPlanEntity(name = "Normalwoche")
        )
        assertTrue(id > 0)

        val plans = weekPlanDao.getAll().first()
        assertEquals(1, plans.size)
        assertEquals("Normalwoche", plans[0].name)
    }

    @Test
    fun weekPlanDao_insertSlotsAndRetrieveWithRelation() = runTest {
        val actId = activityDao.insert(
            ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast")
        )
        val weekPlanId = weekPlanDao.insert(
            WeekPlanEntity(name = "Normalwoche")
        )

        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 1,
                timeOfDay = 0,
                activityId = actId
            )
        )
        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 2,
                timeOfDay = 0,
                activityId = actId
            )
        )

        val weekPlanWithSlots = weekPlanDao.getWeekPlanWithSlotsOnce(weekPlanId)
        assertNotNull(weekPlanWithSlots)
        assertEquals(2, weekPlanWithSlots!!.slots.size)
    }

    @Test
    fun weekPlanDao_uniqueConstraint_dayAndTimeOfDay() = runTest {
        val weekPlanId = weekPlanDao.insert(
            WeekPlanEntity(name = "Normalwoche")
        )
        val actId = activityDao.insert(
            ActivityEntity(name = "Test", iconRef = "ic_test")
        )

        // First insert succeeds
        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 1,
                timeOfDay = 0,
                activityId = actId
            )
        )

        // Second insert with REPLACE strategy overwrites (same unique key)
        val actId2 = activityDao.insert(
            ActivityEntity(name = "Test2", iconRef = "ic_test2")
        )
        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 1,
                timeOfDay = 0,
                activityId = actId2
            )
        )

        // Should still be just 1 slot (replaced, not duplicated)
        val weekPlan = weekPlanDao.getWeekPlanWithSlotsOnce(weekPlanId)
        assertNotNull(weekPlan)
        assertEquals(1, weekPlan!!.slots.size)
        assertEquals(actId2, weekPlan.slots[0].activityId)
    }

    @Test
    fun weekPlanDao_setNull_onPlanDelete() = runTest {
        val planId = planDao.insert(
            RoutinePlanEntity(name = "Morgenroutine", slotCount = 4)
        )
        val weekPlanId = weekPlanDao.insert(
            WeekPlanEntity(name = "Normalwoche")
        )

        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 1,
                timeOfDay = 0,
                routinePlanId = planId
            )
        )

        // Delete the referenced routine plan
        planDao.deleteById(planId)

        // Slot should still exist, but routinePlanId should be null
        val weekPlan = weekPlanDao.getWeekPlanWithSlotsOnce(weekPlanId)
        assertNotNull(weekPlan)
        assertEquals(1, weekPlan!!.slots.size)
        assertNull(weekPlan.slots[0].routinePlanId)
    }

    @Test
    fun weekPlanDao_cascadeDelete_weekPlanRemovesSlots() = runTest {
        val weekPlanId = weekPlanDao.insert(
            WeekPlanEntity(name = "Normalwoche")
        )

        weekPlanDao.insertSlot(
            WeekPlanSlotEntity(
                weekPlanId = weekPlanId,
                dayOfWeek = 1,
                timeOfDay = 0
            )
        )

        weekPlanDao.delete(WeekPlanEntity(id = weekPlanId, name = "Normalwoche"))

        val result = weekPlanDao.getWeekPlanWithSlotsOnce(weekPlanId)
        assertNull(result)
    }
}
