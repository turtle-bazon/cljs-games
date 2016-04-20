var listener = null;
extension.setMessageListener(function (msg) {
    if (listener instanceof Function) {
        listener(msg);
    };
});
exports.hideLaunchScreen = function (msg, callback) {
    listener = callback;
    extension.postMessage(msg);
};
exports.hideLaunchScreenSync = function (msg) {
    return extension.internal.sendSyncMessage(msg);
};
