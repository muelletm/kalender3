# kalender3
Advent calendar android app.

This project is a classic advent calendar.

Meaning that for each of the first 24 days of december there a door (window)
that the user can open on the respective day to see a suprise.

This project just contains the source it should be completed by adding
images and audio files to the resource directory.

Images should live in `kalender3/app/src/main/res/drawable`. Audio files
should live in `kalender3/app/src/main/res/raw`.

There is a simple naming scheme that will be used to assign resources to a
specific day and resource type.

The schema is `{type}_{day}[_.*].extension`. 
Where type is one of `door`, `image`, `audio` and day is an integer (with optional leading zeros.).

These are some valid examples:

 * `image_05_a_tree.jpg`
 * `door_17.jpg`
 * `audio_05_a_christmas_song.ogg`
 
The `door` resource will be used to represent the doors of each day.
Images and audio files are the hidden suprises.
