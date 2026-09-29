# Gituser App 🚀
Developed using Android Studio Quail 1 2026.1.1 Patch 2:
- MVVM + Clean Architecture
- Multi Module
- Kotlin XML + Jetpack Libraries (LiveData + ViewModel) + Glide + RecylcerView
- Koin
- Unit Test + UI Test
- Retrofit + Okhttp + Moshi + Chucker
- Coroutines
- Room


## Improvement:
- Dual Mode Users Screen: integrate Search Screen and User List Screen into single screen. If Search Bar is empty then it became User List Screen mode, otherwise it became Search Screen mode.
- pagination on recylerview: auto fetch next page if last item visible on screen
- show error information when bad network happen
- caching list users every fetch from endpoint GET /users and GET /search/users
- offline mode first on User List Screen mode
- Search Screen mode automaticly switch to offline mode (use list user cache on room) if HTTP request failed (for improve UX purpose)