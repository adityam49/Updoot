Draggable FAB for scrolling         |Comments
:----------------------------------:|:----------------------------------:
![](https://i.imgur.com/iAfLVBM.gif)|![](https://i.imgur.com/jmTx2RZ.png)

# Development
To build this app you need to have a Reddit account.  
Once you have that, head over to [this](https://www.reddit.com/prefs/apps) page to get a client id for the app.  
Make a new installed type app with appropriate information.  

Now on your development machine goto 
- Windows: `C:\Users\<Your Username>\.gradle`
- Mac: `/Users/<Your Username>/.gradle`
- Linux: `/home/<Your Username>/.gradle`  
  
and then add the following 2 lines to gradle.properties file (create one if it doesn’t exist).  

`UPDOOT_CLIENT_ID = "xxxxxxxxxxxxxx"`  
`UPDOOT_REDIRECT_URI = "https://redirecturl.com"`

where `xxxxxxxxxxxxxx` is the client id that you got after creating a new installed type app  
and `https://redirecturl.com` is the redirect url spicified while creating the installed type app.    

For further information on Reddit’s oauth usage use [this guide](https://github.com/reddit-archive/reddit/wiki/oauth2).
