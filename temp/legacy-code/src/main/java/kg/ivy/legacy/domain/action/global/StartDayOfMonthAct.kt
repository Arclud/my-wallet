package kg.ivy.wallet.domain.action.global

import kg.ivy.frp.action.FPAction
import kg.ivy.frp.then
import kg.ivy.legacy.IvyWalletCtx
import kg.ivy.base.legacy.SharedPrefs
import javax.inject.Inject

class StartDayOfMonthAct @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val ivyWalletCtx: IvyWalletCtx
) : FPAction<Unit, Int>() {

    override suspend fun Unit.compose(): suspend () -> Int = suspend {
        sharedPrefs.getInt(SharedPrefs.START_DATE_OF_MONTH, 1)
    } then { startDay ->
        ivyWalletCtx.setStartDayOfMonth(startDay)
        startDay
    }
}
