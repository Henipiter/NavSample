package com.example.navsample

import com.example.navsample.entities.relations.PriceByCategory
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChartHelperTest {

    private val chartHelper = ChartHelper()

    @Test
    fun testCreatingTimelineData() {
        //given
        val list = ArrayList<PriceByCategory>()
        list.add(PriceByCategory(100F, "FOOD", "2023-09"))
        list.add(PriceByCategory(10F, "FOOD", "2023-11"))
        list.add(PriceByCategory(20F, "SPORT", "2023-10"))
        //when
        val result = chartHelper.createTimelineData("2023-09", "2023-11", list)
        //then
        assertAll({
            assertEquals(2, result.size)
            assertEquals(arrayListOf(100F, 0F, 10F), result[0])
            assertEquals(arrayListOf(0F, 20F, 0F), result[1])
        })

    }

    @Test
    fun testCreatingTimelineData2() {
        //given
        val list = ArrayList<PriceByCategory>()
        list.add(PriceByCategory(100F, "FOOD", "2023-09"))
        list.add(PriceByCategory(10F, "FOOD", "2023-11"))
        list.add(PriceByCategory(20F, "SPORT", "2023-10"))
        //when
        val result = chartHelper.createTimelineData("2023-10", "2023-11", list)
        //then
        assertAll({
            assertEquals(2, result.size)
            assertEquals(arrayListOf(0F, 10F), result[0])
            assertEquals(arrayListOf(20F, 0F), result[1])
        })

    }

}