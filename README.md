# HvZHub Mobile  
HvZHub Mobile is a companion app for the http://www.HvZHub.com/ website, which can be used to organize Humans vs Zombies games.  
For more information about Humans vs Zombies, visit http://www.humansvszombies.org/  
For more information about HvZHub, visit http://www.HvZHub.com/   
  
## Screenshots
For screenshots and other assets, check out the [README in the `Images` directory](./Images/)

## Features
- **Chat:** Chat with fellow humans(or zombies if that's your thing). Now with push notifications!
- **Mission Updates:** Stay up to date with the latest mission announcements
- **Game News:** See when people get tagged.
- **Report a tag:** Eat some tasty brains.
- **View your code:** But don't forget to write it down on an index card too.
- **Heatmap:** See where zombies have fed in the past. Stay away from the hotspots so you don't get tagged. The heatmap will slowly get more accurate as more tags are made.

Note: This app is still in beta. If you find something that doesn't work, or even if just have a suggestion, please send us a message using the 'Send Feedback' button in the app.

## Downloads
You can get the app [on the play store](https://play.google.com/store/apps/details?id=com.hvzhub.app&hl=en), or from the [releases](https://github.com/dst33nburgh/HvZHub-App/releases) tab.

## Development
In the past, we created a branch for each feature, and merged them into `master` upon completion. Moving forward, we'll be primarily branching off of `development`, and using `master` only for code that is ready to be released. If you'd like to contribute, feel free to fork the project!

#### Heatmap
Our heatmap uses the google maps API. If you want this feature to work, you'll need to add a google maps API key to /res/google_maps_api.xml

#### API
HvZHub Mobile interfaces with the [HvZHub.com](http://www.hvzhub.com/) backend via a JSON API developed by the HvZHub.com [dev team](http://hvzhub.com/about). We use Retrofit 2.0 for all of our API calls along with a few helper methods we've tacked on. An example of an API call can be found in [LoginActivity](https://github.com/steenburgh/HvZHub-Mobile/blob/master/app/src/main/java/com/hvzhub/app/LoginActivity.java#L103)

#### Push Notifications.
We use Google Cloud Messaging(GCM) for push notifications. Our code for GCM is primarily located in [GcmRegIntentService](https://github.com/steenburgh/HvZHub-Mobile/blob/master/app/src/main/java/com/hvzhub/app/GCMRegIntentService.java) and [HvZHubGcmListenerService](https://github.com/steenburgh/HvZHub-Mobile/blob/master/app/src/main/java/com/hvzhub/app/HvZHubGcmListenerService.java). 


