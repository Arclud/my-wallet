package kg.ivy.data.repository.fake

import kg.ivy.base.TestDispatchersProvider
import kg.ivy.data.DataObserver
import kg.ivy.data.repository.RepositoryMemoFactory
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
fun fakeRepositoryMemoFactory(): RepositoryMemoFactory = RepositoryMemoFactory(
    dataObserver = DataObserver(),
    dispatchers = TestDispatchersProvider
)