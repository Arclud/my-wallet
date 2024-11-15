package kg.ivy.legacy.domain.action.viewmodel.home

import kg.ivy.frp.action.FPAction
import kg.ivy.base.legacy.SharedPrefs
import javax.inject.Inject

class ShouldHideIncomeAct @Inject constructor(
    private val sharedPrefs: SharedPrefs
) : FPAction<Unit, Boolean>() {
    override suspend fun Unit.compose(): suspend () -> Boolean = {
        sharedPrefs.getBoolean(
            SharedPrefs.HIDE_INCOME,
            false
        )
    }
}
