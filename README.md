# LocationApp
Application to get Location Coordinates

## Description
This is an app developed by myself as an R&D task assigned to me by Kingslake during my Internship as a Software Developer at the place.
The app uses Google location api.
There are 2 options
  ### Start Location Service
  * Location of the device is continuously retrieved and stored in a text file in system files in the format "<latitude>,<longitude>,<unixTime>". Each entry is separated by a "|" and each restart of the service is separated by a couple of "\n".
  * The service runs as a foreground service once started and will continue running even if application is closed.
  * A notification is displayed during it's active period.
  
  ### Get Current Location
  * Current Live Location is retrieved and displayed in a Toast message. Can be activated despite if service is running or not.
