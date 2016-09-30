# Kiwi Explorer
Jordan Crawford, 1196240

COMP448, Semester B, 2016

Targeted for Android Lollipop and higher.

## What is this app?
"Kiwi Explorer" allows users to track which major New Zealand cities they have been to.

The app tracks the users location. If they enter a major city, the app marks this as a city they have seen. User's can also manually change which cities they have seen (so the app is still useful if location services are not enabled).

User's can touch a city to see it's location on a map.

## How does the app work?
An always running background service watches for location updates. This updates the seen cities and current city in the database. This service starts with the phone and detects changes to the user's location settings.

Database updates notify the main activity (if it is running) so the view is refreshed when the service makes a change. The main activity is responsible for ensuring the app has the required permissions and location is enabled.

## Testing the app
The automatic seen city updating feature uses the current location. To test this, a fake GPS app can be used. One example of such an app is [Fake GPS Location Spoofer](https://play.google.com/store/apps/details?id=com.incorporateapps.fakegps.fre&hl=en).