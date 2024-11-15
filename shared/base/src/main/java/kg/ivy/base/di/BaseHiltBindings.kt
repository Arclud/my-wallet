package kg.ivy.base.di

import kg.ivy.base.resource.AndroidResourceProvider
import kg.ivy.base.resource.ResourceProvider
import kg.ivy.base.threading.DispatchersProvider
import kg.ivy.base.threading.IvyDispatchersProvider
import kg.ivy.base.time.TimeConverter
import kg.ivy.base.time.TimeProvider
import kg.ivy.base.time.impl.DeviceTimeProvider
import kg.ivy.base.time.impl.StandardTimeConverter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface BaseHiltBindings {
    @Binds
    fun dispatchersProvider(impl: IvyDispatchersProvider): DispatchersProvider

    @Binds
    fun bindTimezoneProvider(impl: DeviceTimeProvider): TimeProvider

    @Binds
    fun bindTimeConverter(impl: StandardTimeConverter): TimeConverter

    @Binds
    fun resourceProvider(impl: AndroidResourceProvider): ResourceProvider
}