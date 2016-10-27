## Changelog

### Version 1.2.1:
- Update dependencies and build tools for SDK 25

### Version 1.2.0:
- Option to show the spinner or not when loading the video

### Version 1.1.5:
- Try to fix looping again, for some devices

### Version 1.1.4:
- Improve looping

### Version 1.1.3:
- Fix an `IllegalArgumentException`

### Version 1.1.2:
- Forgot to ensure the progress bar is visible.

### Version 1.1.1:
- Add progress bar back to the view if there isn't one when starting a video. Helpful for recycling with lists.
- Throw exception if you haven't released the original video when you try to start a new one.

### Version 1.1.0:
- Fix performance issues by setting the data source from a background thread
- Improve the loading UI
- Create an error listener interface

### Version 1.0.4:
- Catch an exception

### Version 1.0.3:
- Make sure the progress spinner is centered

### Version 1.0.2:
- Some fixes for placing within a List

### Version 1.0.0:
- Initial Release
