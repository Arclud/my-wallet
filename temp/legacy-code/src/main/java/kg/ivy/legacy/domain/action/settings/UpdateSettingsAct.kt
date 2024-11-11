package kg.ivy.legacy.domain.action.settings

import kg.ivy.data.db.dao.write.WriteSettingsDao
import kg.ivy.frp.action.FPAction
import kg.ivy.legacy.datamodel.Settings
import javax.inject.Inject

class UpdateSettingsAct @Inject constructor(
    private val writeSettingsDao: WriteSettingsDao
) : FPAction<Settings, Settings>() {
    override suspend fun Settings.compose(): suspend () -> Settings = suspend {
        writeSettingsDao.save(this.toEntity())
        this
    }
}
