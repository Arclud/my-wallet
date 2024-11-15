package kg.ivy.search

sealed interface SearchEvent {
    data class Search(val query: String) : SearchEvent
}
