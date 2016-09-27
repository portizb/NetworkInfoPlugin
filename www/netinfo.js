/*global cordova, module*/

module.exports = {
    lookup: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NetworkInfoPlugin", "lookup", []);
    }
};
