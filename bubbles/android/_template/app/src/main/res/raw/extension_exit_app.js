var listener = null;
extension.setMessageListener(function (msg) {
    if (listener instanceof Function) {
        listener(msg);
    };
});
exports.exitApp = function (msg, callback) {
    listener = callback;
    extension.postMessage(msg);
};
