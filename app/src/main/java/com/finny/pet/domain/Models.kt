package com.finny.pet.domain

enum class PeriodState { LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED, REPLAY_AVAILABLE }
enum class PetStage { BABY, GROWING, ADULT }
enum class PetEmotion { JOY, SADNESS, SURPRISE, SLEEP, EATING, PLAYING }

data class PetState(val stage: PetStage = PetStage.BABY, val emotion: PetEmotion = PetEmotion.JOY)
data class GameState(val id: String, val period: Int, val completed: Boolean = false, val coins: Int = 0, val stars: Int = 0, val errors: Int = 0)
data class Reward(val coins: Int, val stars: Int)
data class BudgetResult(val income: Int, val expenses: Int, val balance: Int, val accepted: Boolean)

data class MoneyCard(val id: String, val title: String, val amount: Int, val income: Boolean)
data class PriorityCard(val id: String, val title: String, val price: Int, val required: Boolean, val target: String = if(required) "NOW" else "LATER")
data class ShopItem(val id: String, val title: String, val price: Int, val required: Boolean, val icon: String = "★")
