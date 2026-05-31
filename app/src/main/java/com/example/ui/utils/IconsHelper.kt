package com.example.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconsHelper {
    val icons = mapOf(
        "home" to Icons.Filled.Home,
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "shopping-cart" to Icons.Filled.ShoppingCart,
        "restaurant" to Icons.Filled.Restaurant,
        "directions_car" to Icons.Filled.DirectionsCar,
        "flight" to Icons.Filled.Flight,
        "local_hospital" to Icons.Filled.LocalHospital,
        "stethoscope" to Icons.Filled.LocalHospital,
        "school" to Icons.Filled.School,
        "work" to Icons.Filled.Work,
        "attach_money" to Icons.Filled.AttachMoney,
        "category" to Icons.Filled.Category,
        "account_balance" to Icons.Filled.AccountBalance,
        "landmark" to Icons.Filled.AccountBalance,
        "credit_card" to Icons.Filled.CreditCard,
        "credit-card" to Icons.Filled.CreditCard,
        "savings" to Icons.Filled.Savings,
        "piggy_bank" to Icons.Filled.Savings,
        "piggy-bank" to Icons.Filled.Savings,
        "money_off" to Icons.Filled.MoneyOff,
        "account_balance_wallet" to Icons.Filled.AccountBalanceWallet,
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "payments" to Icons.Filled.Payments,
        "monetization_on" to Icons.Filled.MonetizationOn,
        "coins" to Icons.Filled.MonetizationOn,
        "lock" to Icons.Filled.Lock,
        "vault" to Icons.Filled.Lock,
        "business_center" to Icons.Filled.BusinessCenter,
        "briefcase" to Icons.Filled.BusinessCenter,
        "bus" to Icons.Filled.DirectionsBus,
        "paw_print" to Icons.Filled.Pets,
        "paw-print" to Icons.Filled.Pets,
        "paw" to Icons.Filled.Pets,
        "pet" to Icons.Filled.Pets,
        "more_horizontal" to Icons.Filled.MoreHoriz,
        "more-horizontal" to Icons.Filled.MoreHoriz,
        "more" to Icons.Filled.MoreHoriz,
        "gem" to Icons.Filled.Star,
        "film" to Icons.Filled.Movie,
        "baby" to Icons.Filled.ChildCare,
        "child" to Icons.Filled.ChildCare,
        "heart" to Icons.Filled.Favorite
    )

    fun getIcon(name: String): ImageVector {
        val normalized = name.trim().lowercase().replace("-", "_")
        return icons[normalized] ?: icons[name] ?: Icons.Filled.Category
    }
}
