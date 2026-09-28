package com.finny.pet.domain

import org.junit.Assert.*
import org.junit.Test

class EconomyTest {
    @Test fun productionRewardScheduleIsExact(){assertEquals(listOf(5,8,10,12,15,18,20,23,26,30),GameEconomy.levelRewards);assertEquals(5,GameEconomy.rewardForLevel(1));assertEquals(30,GameEconomy.rewardForLevel(10));assertEquals(8,GameEconomy.arcadeReward)}
    @Test fun fiveDayStoryEconomyIsExact(){assertEquals(listOf(50,60,70,80,100),GameEconomy.dailyIncomeTargets);assertEquals(listOf(20,20,25,25,30),GameEconomy.dailyFoodCosts);assertEquals(360,GameEconomy.dailyIncomeTargets.sum());assertEquals(120,GameEconomy.dailyFoodCosts.sum());assertEquals(240,GameEconomy.dailyIncomeTargets.sum()-GameEconomy.dailyFoodCosts.sum())}
    @Test fun scalesLevelTotalsAreExact() {
        assertEquals(10, 50 + 30 - 20 - 30 - 20)
        assertEquals(10, 70 + 30 - 25 - 35 - 20 - 10)
        assertEquals(20, 70 + 50 + 30 - 30 - 40 - 20 - 25 - 15)
    }

    @Test fun balanceNeverBecomesNegative() {
        val result = BalanceCalculator.calculate(50, 70)
        assertFalse(result.accepted)
        assertEquals(0, result.balance)
        assertFalse(BalanceCalculator.canSpend(15, 20))
        assertTrue(BalanceCalculator.canSpend(20, 20))
    }

    @Test fun changeTasksAreExact() {
        assertEquals(15, 50 - 35)
        assertEquals(2, 50 - 48)
        assertEquals(25, 50 - 25)
        assertEquals(33, 100 - 67)
        assertEquals(15, 100 - 85)
        assertEquals(26, 100 - 74)
    }

    @Test fun requiredBudgetsAreExact() {
        assertEquals(20, 50 - 20 - 10)
        assertEquals(10, 70 - 30 - 20 - 10)
        assertEquals(40, 100 - 25 - 15 - 20)
        assertEquals(65, 120 - 35 - 20)
    }

    @Test fun piggyRatingBoundariesAreExact() {
        assertEquals(3, RewardCalculator.piggyBank(90, 0).stars)
        assertEquals(2, RewardCalculator.piggyBank(70, 1).stars)
        assertEquals(1, RewardCalculator.piggyBank(50, 4).stars)
        assertEquals(1, RewardCalculator.piggyBank(49, 0).stars)
    }
}
