/*global cordova, module*/

module.exports = {
    lookup: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NetworkInfo", "lookup", [name]);
    }
};
