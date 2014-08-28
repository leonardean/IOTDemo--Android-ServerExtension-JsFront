IOTDemo--Android-ServerExtension-JsFront
========================================
There are three demos in each folder in this repo:
* `Android`
* `Server Extension`
* `JsFront`

`Android` folder contains an Eclipse Android project, which can be opened and run directly by Eclipse. Please use account name：evan，password：123456 to sign in. The demo app contains the following functionality:
* Logout
* Add new data
* Retrieve all data
* Analytics by day
* Analytics by week

When adding new data, the app includes: temperature, humidity、UV、Device ID, Geo-location. If you want to use the app for your own KiiCloud service, you need to change `APP_ID`, `APP_KEY` in `Constants.java` and  `Kii.Site`, `KiiAnalytics.Site` in `DemoApplication.java`.

`ServerExtension` folder contains a hook config file `./deploy/wdRecord.hook` and a server function js file `modifyWdRecord.js`. You need to install [Nodejs](http://nodejs.org/) first in order to deploy the two files to Kii Cloud with the following command:
```
node bin/kii-servercode.js deploy-file \
  --file <js_function_file> \
  --site us \
  --app-id <your_app_id> \
  --app-key <your_app_key> \
  --client-id <your_client_id> \
  --client-secret <your_client_secret> \
  --hook-config <hook_file>
```

`JsFront` contains two html pages and javascript files showing how to retrieve analytics results. Please note that there are difference between the demo js code and the code snippet in the IOT blueprint documentaion because the demo code need to take care of user inputs and dynamic REST request generation while the code snippet only shows a fixed request.


