cordova.define("cordova-plugin-sim.Sim", function(require, exports, module) { var sim = {
  getSimInfo: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'Sim', 'getSimInfo', []);
  }
}

cordova.addConstructor(function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.sim = sim;
  return window.plugins.sim;
});

});
