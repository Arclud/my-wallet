package kg.ivy.contributors

sealed interface ContributorsEvent {
    data object TryAgainButtonClicked : ContributorsEvent
}
