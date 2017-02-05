# Android SimpleVideoView

![Screenshot](preview.png)

View the [YouTube Demo](https://youtu.be/NrZ3sNKl-NM), showcasing the libraries ability to be used within a `ListView` or `RecyclerView`.


Google's `VideoView` is great, and powerful, but I was not a fan at all of working with it. It was difficult, complex, overweight, and confusing. It also came with some key issues:

 - Bad aspect ratio and video stretching in some cases
 - Always took control of the `AudioManager` service (on Lollipop+)

This library looks to simplify the `VideoView`. It is just a wrapper of Android's `MediaPlayer` that addresses some of the issues. It is also very light-weight up until you start the actual playback, so, as long as you are smart about starting the videos, you could easily add this to a `RecyclerView` and use it for in-line video playback.

## Features

 - Light-weight and easy to add into `RecyclerView` or `ListView`
 - Correct aspect ratio for videos so they don't get stretched
 - Configurations for whether or not you want to take control of the system's audio
 - Mute video content
 - Built in progress spinner

Note: This library does NOT contain any of the video controls at this time (play, pause, fast-forward, rewind), although it does expose the methods necessary for you to implement them on your own. This may be something I add in the future.

## Installation

There are two ways to use this library:

#### As a Gradle dependency

This is the preferred way. Simply add:

```groovy
dependencies {
    compile 'com.klinkerapps:simple_videoview:1.2.4'
}
```

to your project dependencies and run `./gradlew build` or `./gradlew assemble`.

#### As a library project

Download the source code and import it as a library project in Eclipse. The project is available in the folder **library**. For more information on how to do this, read [here](http://developer.android.com/tools/projects/index.html#LibraryProjects).

## Example Usage

Functionality can be found in the example's [MainActivity](https://github.com/klinker24/Android-SimpleVideoView/blob/master/example/src/main/java/com/klinker/android/simple_videoview_example/MainActivity.java). It is very simple to play a video:

```java
videoView.start("uri to video");
```

Defined in the XML layout:
```xml
<com.klinker.android.simple_videoview.SimpleVideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:loop="true"
    app:stopSystemAudio="true"
    app:muted="false"
    app:showSpinner="true"/>
```

You also need to cleanup after the video is done being viewed. I would recommend doing this in the `Activity#onStop` to aleviate some playback issues. If you do not call the `SimpleVideoView#release` function, there will be leakage everywhere, you will immediately be able to tell something is wrong thanks to the slowdowns it will cause!

```java
@Override
public void onStop() {
    super.onStop();
    videView.release();
}
```

## Example Usage within a List

For an example of using this in a `RecyclerView`, I recommend checking out my app, Talon for Twitter, and it's implementation on the [`GiffySearch`](https://github.com/klinker24/Talon-for-Twitter/blob/master/src/main/java/com/klinker/android/twitter/ui/GiffySearch.java). With this specific example, we do not automatically start the video when they are scrolled to, but, of course, this is totally possible and I prefer the auto-play way. 

I will leave the auto-play implementation up to you all for now though. I cannot give away all my secrets, just the tools to do them. I may add an example for this in the future. Just make sure that you are releasing the videos correctly, as described above!

## Contributing

Please fork this repository and contribute back using [pull requests](https://github.com/klinker24/Android-SimpleVideoView/pulls). Features can be requested using [issues](https://github.com/klinker24/Android-SimpleVideoView/issues). All code, comments, and critiques are greatly appreciated.

## Changelog

The full changelog for the library can be found [here](https://github.com/klinker24/Android-SimpleVideoView/blob/master/changelog.md).


## License

    Copyright 2016 Luke Klinker

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
