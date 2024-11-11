package kg.ivy.releases

sealed interface ReleasesEvent {
    data object OnTryAgainClick : ReleasesEvent
}
