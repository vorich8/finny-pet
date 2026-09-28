package com.finny.pet.domain

interface GameEngine<S, A, R> {
    fun reduce(state: S, action: A): S
    fun calculateResult(state: S): R
}

object BalanceCalculator {
    fun calculate(income: Int, expenses: Int): BudgetResult {
        val balance = income - expenses
        return BudgetResult(income, expenses, balance.coerceAtLeast(0), balance >= 0)
    }
    fun canSpend(balance: Int, amount: Int) = amount >= 0 && amount <= balance
}

object GameEconomy {
    const val arcadeReward = 8
    val levelRewards = listOf(5,8,10,12,15,18,20,23,26,30)
    val dailyIncomeTargets = listOf(50,60,70,80,100)
    val dailyFoodCosts = listOf(20,20,25,25,30)
    fun rewardForLevel(level:Int)=levelRewards[level.coerceIn(1,10)-1]
    fun incomeTarget(day:Int)=dailyIncomeTargets[day.coerceIn(1,5)-1]
    fun foodCost(day:Int)=dailyFoodCosts[day.coerceIn(1,5)-1]
}

object RewardCalculator {
    fun scales(level: Int) = if (level == 1) Reward(10, 1) else Reward(0, 0)
    fun priorities(level: Int) = when(level) { 1 -> Reward(10, 1); 2 -> Reward(15, 0); 3 -> Reward(20, 0); else -> Reward(0, 0) }
    fun goal(cost: Int) = when(cost) { 50 -> Reward(15, 0); 100 -> Reward(25, 0); 150 -> Reward(40, 0); else -> Reward(0, 0) }
    fun shop(period: Int) = when(period) { 2 -> Reward(15, 1); 4 -> Reward(20, 1); else -> Reward(0, 0) }
    fun change() = Reward(20, 1)
    fun piggyBank(percent: Int, temptations: Int) = when {
        percent >= 90 && temptations == 0 -> Reward(0, 3)
        percent >= 70 && temptations <= 1 -> Reward(0, 2)
        percent >= 50 -> Reward(0, 1)
        else -> Reward(0, 1)
    }
}

object PetGrowthCalculator {
    fun stage(completedPeriod: Int): PetStage = when {
        completedPeriod >= 5 -> PetStage.ADULT
        completedPeriod >= 2 -> PetStage.GROWING
        else -> PetStage.BABY
    }
}
