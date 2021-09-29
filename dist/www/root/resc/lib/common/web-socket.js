function WebSocketer() {
    var THIS = this;
    // 是否断网自动重连
    var _retryOnClose = true;
    var _retryInterval = 10 * 1000;
    var _retryTimer = null;
    //
    var _pingTimer = null;
    var _pingInterval = 30 * 1000;
    // REQUEST_PING = 0;
    var _pingText = JSON.encode({
        "code": 0,
        "message": "PING"
    });
    // 实际的对象
    var _webSocket = null;
    // 是否已根据需要手动关闭了
    var _webSocketClosedManually = false;
    //
    var _webSocketUrl = null;
    //
    var _resultHandler = function () {
        console.warn("你没有指定结果回调函数！");
    };

    //
    function sendData(data) {
        if(_webSocket != null) {
            try {
                _webSocket.send(data);
            } catch(ex) {
                console.error(ex);
            }
        }
    }

    function sendPingRequest() {
        sendData(_pingText);
    }

    //
    this.setRetryInterval = function (retryInterval) {
        if(retryInterval === false || retryInterval <= 0) {
            _retryOnClose = false;
        } else {
            _retryOnClose = true;
            if(typeof retryInterval == "number") {
                _retryInterval = retryInterval;
            }
        }
        //
        return this;
    };
    //
    this.setResultHandler = function (resultHandler) {
        if(typeof resultHandler == "function") {
            _resultHandler = resultHandler;
        }
        //
        return this;
    };
    //
    this.send = function (data) {
        sendData(data);
    };
    //
    this.close = function (forReopen) {
        forReopen = forReopen === true;
        if(!forReopen) {
            _webSocketClosedManually = true;
            //
            window.clearInterval(_pingTimer);
        }
        if(_webSocket != null) {
            _webSocket.close();
        }
        //
        return this;
    };
    //
    this.isOpen = function () {
        return _webSocket != null;
    };
    //
    this.open = function (socketUrl, initParams, resultHandler) {
        if(!WebSocketer.isSupported) {
            console.warn("当前浏览器不支持 WebSocket，请更换浏览器");
            return this;
        } else if(_webSocket != null) {
            return this;
        }
        var wsServerBase = "";
        if(socketUrl) {
            if(socketUrl.indexOf("://") == -1) {
                var serverBase = getServerBase();
                if(serverBase.startsWith("https://")) {
                    wsServerBase = replace(serverBase, "https://", "wss://");
                } else if(serverBase.startsWith("http://")) {
                    wsServerBase = replace(serverBase, "http://", "ws://");
                }
            } else if(!socketUrl.startsWith("ws://") && !socketUrl.startsWith("wss://")) {
                if(socketUrl.startsWith("https://")) {
                    socketUrl = replace(socketUrl, "https://", "wss://");
                } else if(socketUrl.startsWith("http://")) {
                    socketUrl = replace(socketUrl, "http://", "ws://");
                }
            }
            console.log("websocket url >> " + (wsServerBase + socketUrl));
        }
        //
        if(socketUrl && initParams) {
            _webSocketUrl = makeUrl(socketUrl, initParams);
        }
        if(typeof resultHandler == "function") {
            _resultHandler = resultHandler;
        }

        //
        _webSocket = new WebSocket(wsServerBase + _webSocketUrl);
        _webSocket.onopen = function (evt) {
            window.clearTimeout(_retryTimer);
            //
            console.log("WebSocket连接 已打开...");
            //
            if(_pingTimer == null) {
                _pingTimer = window.setInterval(sendPingRequest, _pingInterval);
            }
        };
        _webSocket.onclose = function (evt) {
            _webSocket = null;
            //
            var reason = evt.reason || "";

            console.log("WebSocket连接 已关闭. " + (reason ? "原因：" + reason : ""));
            // CANNOT_ACCEPT(1003)
            var closeCode = evt.code;
            //
            if(closeCode != 1003 && _retryOnClose && !_webSocketClosedManually) {
                _retryTimer = window.setTimeout(THIS.open, _retryInterval);
            }
        };
        _webSocket.onerror = function (msg) {
            _webSocket = null;
            //
            console.error("WebSocket连接 发生错误!");
        };
        // 结果数据处理
        _webSocket.onmessage = function (evt) {
            var result = JSON.decode(evt.data || null);
            if(result != null) {
                if(result.requestCode !== 0) {
                    // 非PING/PONG 信息
                    // console.log(result);
                    _resultHandler(result, THIS);
                }
            } else {
                console.log("返回结果无数据");
            }
        };
        //
        return this;
    };
}
//
WebSocketer.isSupported = getWebSocket() != null;

WebSocketer.newOne = function () {
    return new WebSocketer();
};