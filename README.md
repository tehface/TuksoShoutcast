White Label Shoutcast
=====================

White label what?
-----------------
Quite a few people have their own Shoutcast radio station these days.  There are a bunch of players out there either on the web or on the Android marketplace.  The problem is that web pages typically don't play on Android and the marketplace apps have 1000's of other radio stations to choose from.

Wouldn't it be cooler to have your own dedicated app that only plays *your* station?  The goal of this project is two-fold:

1. To make an android app for my friends over at KOUV radio

2. To make this app as generic as possible so that the next developer can drop in a logo, change a few text strings, find/replace the package name and send his friends their own player.

Current status
--------------
The player is in a basic working state.  You can start/stop the stream.  When the stream is started, a notification is placed in the Notification Bar.  Stopping the stream removes the notification.

How to customize
----------------
* Edit the 5 strings in brandable.xml, once of them is the address to your stream
* Change the logo
* Change the icon (it's from openclipart.org if you are looking for more)
* Find/replace the package name com.shoutcastwhitelabel.player with your package name
* Scream "booyah"

Credits
-------
Huge thanks to the original author that I forked from, https://github.com/Dawnthorn/nagare

License
-------
MIT