package com.finny.pet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.finny.pet.ui.games.*
import com.finny.pet.ui.screens.*
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable fun FinnyNavHost() {
    val nav= rememberNavController()
    NavHost(navController=nav,startDestination=Routes.Bootstrap){
        composable(Routes.Bootstrap){BootstrapScreen(onFirstLaunch={nav.navigate(Routes.Onboarding){popUpTo(Routes.Bootstrap){inclusive=true}}},onReady={nav.navigate(Routes.Home){popUpTo(Routes.Bootstrap){inclusive=true}}})}
        composable(Routes.Onboarding){OnboardingScreen{nav.navigate(Routes.CreatePet){popUpTo(Routes.Onboarding){inclusive=true}}}}
        composable(Routes.CreatePet){CreatePetScreen(onDone={nav.navigate(Routes.Tutorial){popUpTo(Routes.CreatePet){inclusive=true}}})}
        composable(Routes.Tutorial){MoneyTutorialScreen{nav.navigate(Routes.Home){popUpTo(Routes.Tutorial){inclusive=true}}}}
        composable(Routes.Home){HomeScreen(nav)}
        composable(Routes.DaySummary){DaySummaryScreen(onBack={nav.popBackStack()},onNext={final->nav.navigate(if(final)Routes.WeekFinal else Routes.Home){popUpTo(Routes.Home){inclusive=true}}})}
        composable(Routes.WeekFinal){WeekFinalScreen(onHome={nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}})}
        composable(Routes.Pet){PetScreen(onBack={nav.popBackStack()},onInventory={nav.navigate(Routes.Inventory)},onQuickGame={nav.navigate(Routes.building("work"))})}
        composable(Routes.Adult){AdultScreen(onBack={nav.popBackStack()},onReset={nav.navigate(Routes.CreatePet){popUpTo(Routes.Home){inclusive=true};launchSingleTop=true}})}
        composable(Routes.Inventory){InventoryScreen(onBack={nav.popBackStack()})}
        composable(Routes.Building,arguments=listOf(navArgument("id"){type=NavType.StringType})){entry->BuildingScreen(entry.arguments?.getString("id")?:"home",onBack={nav.popBackStack()},navigate={nav.navigate(it)})}
        composable(Routes.ExtendedGame,arguments=listOf(navArgument("gameId"){type=NavType.StringType})){ExtendedGameScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.EducationalGame,arguments=listOf(navArgument("gameId"){type=NavType.StringType},navArgument("level"){type=NavType.IntType})){entry->
            val id=entry.arguments?.getString("gameId")?:"scales"
            val back={nav.popBackStack();Unit};val done:(Int)->Unit={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}}
            when(id){
                "scales"->ScalesScreen(back,done);"priorities"->PrioritiesScreen(back,done);"piggy"->PiggyBankScreen(back,done)
                "goal"->GoalScreen(back,done);"shop"->ShopScreen(back,done);"change"->ChangeScreen(back,done)
                else->ExtendedGameScreen(back,done)
            }
        }
        composable(Routes.ArcadeGame,arguments=listOf(navArgument("gameId"){type=NavType.StringType})){entry->ArcadeGameScreen(kind=entry.arguments?.getString("gameId")?:"tetris",onBack={nav.popBackStack()},onDone={nav.navigate(Routes.ArcadeResult)})}
        composable(Routes.ArcadeResult){ArcadeResultScreen{nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}}}
        composable(Routes.Result,arguments=listOf(navArgument("stars"){type=NavType.IntType})){entry->ResultScreen(stars=entry.arguments?.getInt("stars")?:1,onHome={nav.navigate(Routes.Home){popUpTo(Routes.Home){inclusive=true}}},onNext={id,level->nav.navigate(Routes.educationalGame(id,level)){popUpTo(Routes.Result){inclusive=true}}})}
        composable(Routes.Scales){ScalesScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.Priorities){PrioritiesScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.PiggyBank){PiggyBankScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.Goal){GoalScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.Shop){ShopScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
        composable(Routes.Change){ChangeScreen(onBack={nav.popBackStack()},onDone={nav.navigate(Routes.result(it)){popUpTo(Routes.Home){inclusive=false}}})}
    }
}
