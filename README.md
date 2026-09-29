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


## Improvement & Additional Features:
- Dual Mode Users Screen: integrate Search Screen and User List Screen into single screen. If Search Bar is empty then it became User List Screen mode, otherwise it became Search Screen mode.
- pagination on recylerview: auto fetch next page if last item visible on screen
- show error information when bad network happen
- caching list users every fetch from endpoint GET /users and GET /search/users
- offline mode first on User List Screen mode
- Search Screen mode automaticly switch to offline mode (use list user cache on room) if HTTP request failed (for improve UX purpose)

## Challanges:
- Initially, I built this using Jetpack Compose because the job poster requirements specified it. But I realized that the test requierement is different, it required a RecyclerView and mentioned LiveData(which is commonly used in Kotlin XML, not compose), so I had to start over and build it using Kotlin XML
- Building a proper, multi-module project using Kotlin XML takes a significant amount of time.

## Why Choose MVVM + Clean Architecture:
    MVVM + Clean was chosen because the majority of the industry uses this approach when working with Kotlin XML (non-compose) and easier for testing
    
    
