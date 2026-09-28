package com.finny.pet.navigation

object Routes {
    const val Bootstrap="bootstrap"; const val Onboarding="onboarding"; const val CreatePet="create_pet"; const val Tutorial="tutorial"; const val Home="home"; const val Pet="pet"; const val Result="result/{stars}"; fun result(stars:Int)="result/${stars.coerceIn(1,3)}"; const val Adult="adult"
    const val Scales="game/scales"; const val Priorities="game/priorities"; const val PiggyBank="game/piggy"; const val Goal="game/goal"; const val Shop="game/shop"; const val Change="game/change"
    const val Building="building/{id}"; fun building(id:String)="building/$id"; const val Inventory="inventory"
    const val DaySummary="day_summary";const val WeekFinal="week_final"
    const val ExtendedGame="game/extended/{gameId}"; fun extendedGame(gameId:String)="game/extended/$gameId"
    const val EducationalGame="game/play/{gameId}/{level}"; fun educationalGame(gameId:String,level:Int)="game/play/$gameId/${level.coerceIn(1,10)}"
    const val ArcadeGame="arcade/{gameId}"; fun arcadeGame(gameId:String)="arcade/$gameId"; const val ArcadeResult="arcade_result"
}
