/*global cordova, module*/

module.exports = {
    lookup: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NetworkInformation", "lookup", [name]);
    }
};
