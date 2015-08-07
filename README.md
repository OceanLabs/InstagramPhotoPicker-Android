# Android Instagram Photo Picker

A Instagram image picker providing a simple UI for a user to pick photos from their Instagram account. 

It takes care of all authentication with Instagram as and when necessary. It will automatically renew auth tokens or prompt the user to re-authorize the app if needed. 

## Video Preview

[![Preview](https://github.com/OceanLabs/InstagramPhotoPicker-Android/raw/master/screenshot.png)](https://vimeo.com/135676657)


## Requirements

* Android API Level 14 - Android 4.0 (ICE_CREAM_SANDWICH)

## Installation
### Android Studio / Gradle

We publish builds of our library to the Maven central repository as an .aar file. This file contains all of the classes, resources, and configurations that you'll need to use the library. To install the library inside Android Studio, you can simply declare it as dependecy in your build.gradle file.

```java
dependencies {
    compile 'ly.kite:instagram-photo-picker:1.+@aar'
}
```

Once you've updated your build.gradle file, you can force Android Studio to sync with your new configuration by selecting Tools -> Android -> Sync Project with Gradle Files

This should download the aar dependency at which point you'll have access to the API calls. If it cannot find the dependency, you should make sure you've specified mavenCentral() as a repository in your build.gradle

## Usage

You need to have set up your application correctly to work with Instagram by registering a new Instagram application here: https://instagram.com/developer/ . For the redirect uri use something link `your-app-scheme://instagram-callback`.

To launch the Instagram Photo Picker:

```java
// Somewhere in an Activity:

import ly.kite.instagramphotopicker.InstagramPhoto;
import ly.kite.instagramphotopicker.InstagramPhotoPicker;

static final String CLIENT_ID = "YOUR_CLIENT_ID";
static final String REDIRECT_URI = "YOUR-APP-SCHEME://instagram-callback";
static final int REQUEST_CODE_INSTAGRAM_PICKER = 1;

InstagramPhotoPicker.startPhotoPickerForResult(this, CLIENT_ID, REDIRECT_URI, REQUEST_CODE_INSTAGRAM_PICKER);
```

Implement `onActivityResult`:

```java

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_INSTAGRAM_PICKER) {
        if (resultCode == Activity.RESULT_OK) {
            Parcelable[] photos = data.getParcelableArrayExtra(InstagramPhotoPicker.EXTRA_SELECTED_PHOTOS);
            InstagramPhoto[] instagramPhotos = new InstagramPhoto[photos.length];
            System.arraycopy(photos, 0, instagramPhotos, 0, photos.length);
            Log.i("dbotha", "User selected " + instagramPhotos.length + " Instagram photos");
            for (int i = 0; i < instagramPhotos.length; ++i) {
                Log.i("dbotha", "Photo: " + instagramPhotos[i].getFullURL());
            }

         }
    }
}
    
```

### Sample Apps
The project is bundled with a Sample App to highlight the libraries usage.

## License
This project is available under the MIT license. See the [LICENSE](LICENSE) file for more info.