package kg.ivy.ui.di

import kg.ivy.ui.time.DevicePreferences
import kg.ivy.ui.time.TimeFormatter
import kg.ivy.ui.time.impl.AndroidDateTimePicker
import kg.ivy.ui.time.impl.AndroidDevicePreferences
import kg.ivy.ui.time.impl.DateTimePicker
import kg.ivy.ui.time.impl.IvyTimeFormatter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface IvyUiBindings {
    @Binds
    fun timeFormatter(impl: IvyTimeFormatter): TimeFormatter

    @Binds
    fun deviceTimePreferences(impl: AndroidDevicePreferences): DevicePreferences

    @Binds
    fun dateTimePicker(impl: AndroidDateTimePicker): DateTimePicker
}