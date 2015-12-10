# Cordova-Plugin-FileChooser

The purpose of this plugin is to provide a file chooser for Hybrid Applications running on Android 4.4.2 (KitKat). With this plugin the users can succussful upload files through a webview.

This plugin was created as a workaround for a known issue with the file input element on webviews running on Android 4.4.2 (KitKat). Thus, we recommend you to only use this plugin for that Android version.

As with all the cordova plugins, the plugin isn't available until the execution of `deviceready` event.

## Supported Platforms

- Android 4.4.2

## Installation
- Run the following command:

```shell
    cordova plugin add https://github.com/OutSystems/Cordova-Plugin-FileChooser.git	
``` 

## Usage

__Parameters__:

- __inputFileOptions__: A JSON object with the following content:

```javascript
{
  "accept" : "string",
  "capture" : "boolean"
}
 
```

- __successCallback__: A callback that is passed when the user select a file with success. _(Function)_

- __errorCallback__: A callback that executes if an error occurs. _(Function)_


### Example 1

```javascript

var success = function (uri) {
    alert(uri);
}

var fail = function (error) {
    alert(error);
}

var options = {"accept":"*","capture":false};

window.filechooser.open(options,success,fail);

```

### Example 2

```javascript

var success = function (uri) {
    alert(uri);
}

var fail = function (error) {
    alert(error);
}

var options = {"accept":"image/*","capture":true};

window.filechooser.open(options,success,fail);

```

###Copyright OutSystems, 2015
