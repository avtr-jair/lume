package com.example.lume.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Receiver : Screen("receiver") // Only reached via Intent? Or navigation?
    object Insights : Screen("insights")
    object Gastos : Screen("gastos")
    object Perfil : Screen("perfil")
    object Scan : Screen("scan")
    object ManageCategories : Screen("manage_categories")
    object CreateCategory : Screen("create_category")
    object ManageAccounts : Screen("accounts")
    object SelectAccountType : Screen("select_account_type")
    object CreateAccount : Screen("create_account/{typeId}")
}
