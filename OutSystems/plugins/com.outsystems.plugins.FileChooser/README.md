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

var success = function (data) {
    console.log(data);
}

var fail = function (error) {
    console.log(error);
}

var options = {"accept":"*","capture":false};

window.filechooser.open(options,success,fail);

```

### Example 2

```javascript

var success = function (data) {
    console.log(data);
}

var fail = function (error) {
    console.log(error);
}

var options = {"accept":"image/*","capture":true};

window.filechooser.open(options,success,fail);

```

###Copyright OutSystems, 2015

---

LICENSE
=======


    [The MIT License (MIT)](http://www.opensource.org/licenses/mit-license.html)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.


Portions of FileUtils.java:

    Copyright (C) 2011 - 2013 Paul Burke

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Portions of FileUtils.java:

    Copyright (C) 2007-2008 OpenIntents.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

LocalStorageProvider.java:

	Copyright (c) 2013, Ian Lake
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
	- Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


