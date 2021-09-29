//type,title,content,skin,area,maxWidth,offset,icon,btn,closeBtn,shade,shadeClose,time,shift,maxmin(1,2),fix,scrollbar,zIndex,tips,tipsMore
//
//layer.open(options) - 原始核心方法
//layer.alert(content, options, yes) - 普通信息框
//layer.confirm(content, options, yes, cancel) - 询问框
//layer.msg(content, options, end) - 提示框
//layer.load(icon, options) - 加载层
//layer.tips(content, follow, options) - tips层
//
//当你在iframe页面关闭自身时
//var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
//parent.layer.close(index); //再执行关闭
//
//layer.close(index) - 关闭特定层
//layer.closeAll(type) - 关闭所有层
//layer.style(index, cssStyle) - 重新定义层的样式
//layer.title(title, index) - 改变层的标题
//layer.getChildFrame(selector, index) - 获取iframe页的DOM
//layer.getFrameIndex(windowName) - 获取特定iframe层的索引
//layer.iframeAuto(index) - 指定iframe层自适应
//layer.iframeSrc(index, url) - //重置特定iframe url
//layer.setTop(layero) -置顶当前窗口
//layer.full()、layer.min()、layer.restore() - 手工执行最大小化
//layer.prompt(options, yes) - 输入层
//layer.tab(options) - tab层
//layer.photos(options) - 相册层
//
var Layer = {
    core: {}
};
//
Layer.core.fn = function () {
    var THIS = this;
    var windowWidth = Math.min($(window).width(), $(document).width);
    var layerIndex = -1;
    var closeTimer = null;
    // success, min, full, restore; end, moveEnd
    // btn callback : yes, cancel, btn3,...
    var typeMap = {
        "dialog": 0, // 信息框（默认）
        "page": 1, // 页面层
        "iframe": 2, // iframe层
        "loading": 3, // 加载层
        "tips": 4	// tips层
    };

    var inputTypeMap = {//
        "text": 0,
        "password": 1,
        "mltext": 2
    };

    var merge = (typeof Utils == 'object' && Utils != null) ? Utils.merge : window.merge;

    function inputTypeToFormType(inputType) {
        return typeof inputType == "number" ? inputType : (inputTypeMap[inputType] || 0);
    }

    function getMsgConfig() {
        return {
            title: false,
            border: [1, 0.5, '#666'],
            shade: [0.3],
            shadeClose: true,
            scrollbar: false,//消除浏览器滚动条
            closeBtn: false,
            time: 0
        };
    }

    function getProgressConfig() {
        return {
            title: false,
            icon: "loading",
            area: ['auto', 'auto'],
            shade: [0.3],
            scrollbar: false,
            shadeClose: false,
            time: 0
        };
    }

    function getHtmlConfig() {
        return {
            type: typeMap["page"],
            title: false,
            shade: [0.3],
            shadeClose: false,
            scrollbar: false,
            area: ['auto', 'auto'],
            closeBtn: false
        };
    }

    function getPromptConfig() {
        return {
            scrollbar: false,
            formType: inputTypeToFormType("text"),
            title: "请输入",
            value: ""
        };
    }

    function getTipsConfig() {
        return {
            tipsMore: true,
            style: ['background-color:#9EDD8C; color:#000; border:1px outset #EEEEEE;', '#9EDD8C'],
            maxWidth: 200,
            time: 5000
        };
    }

    function getDialogConfig() {
        return {
            type: typeMap["page"],
            closeBtn: false,
            title: "对话框",
            area: ['auto', 'auto'],
            maxmin: false,
            scrollbar: false,
            // offset : ['40px', ''],
            // shift : 'top',
            // content : 'dom',
            btn: ['确定', '取消']
        };
    }

    function getFrameConfig() {
        return {
            scrollbar: false,
            type: typeMap["iframe"],
            closeBtn: false,
            title: "页面",
            area: ['auto', 'auto'],
            maxmin: true,
            // offset : ['-6px', ''],
            // shift : 'top',
            content: 'about:blank'
        };
    }

    function filterId(domId) {
        if(typeof domId == "string") {
            if(domId.indexOf("#") != 0) {
                domId = "#" + domId;
            }
        }
        return domId;
    }

    function roundTheLayer(radius) {
        var theLayer = $id("layui-layer" + layerIndex);
        var theMain = theLayer;
        var height = typeof radius == "number" ? radius : theMain.height() / 2;
        theMain.css("border-radius", height + "px");
        theMain.css("opacity", 0.9);
    }

    //
    this.hide = function () {
        layer.close(layerIndex);
        layerIndex = -1;
        //
        return this;
    };
    this.hidePrevious = function () {
        clearTimeout(closeTimer);
        if(layerIndex != -1) {
            this.hide();
        }
        //
        return this;
    };
    //
    this.info = function (msg, callback) {
        this.hidePrevious();
        //
        layerIndex = layer.alert(msg, {
            title: "提示",
            icon: "info",
            btn: "知道了"
        }, callback);
        //
        return this;
    };
    this.warning = function (msg, callback) {
        this.hidePrevious();
        //
        layerIndex = layer.alert(msg, {
            title: "提示",
            icon: "warning",
            btn: "知道了"
        }, callback);
        //
        return this;
    };
    this.error = function (msg, callback) {
        this.hidePrevious();
        //
        layerIndex = layer.alert(msg, {
            title: "提示",
            icon: "error",
            btn: "知道了"
        }, callback);
        //
        return this;
    };
    this.progress = function (msg) {
        this.hidePrevious();
        //
        var config = getProgressConfig();
        //
        layerIndex = layer.msg(msg || "请稍后...", config);
        //
        roundTheLayer();
        //
        return this;
    };
    this.loading = function () {
        this.hidePrevious();
        //
        layerIndex = layer.load(0);
        //
        return this;
    };
    this.confirm = function (msg, yesCallback, noCallback) {
        this.hidePrevious();
        //
        layerIndex = layer.confirm(msg, {
            title: "提示",
            icon: "confirm"
        }, yesCallback, noCallback);
        //
        return this;
    };
    this.prompt = function (config, yesCallback) {
        this.hidePrevious();
        //
        config = config || {};
        if(typeof config == "string") {
            config = {
                title: config
            };
        }
        if(config.inputType != null) {
            config.formType = inputTypeToFormType(config.inputType);
        }
        var theConfig = merge(getPromptConfig(), config);
        //
        layerIndex = layer.prompt(theConfig, yesCallback);
        //
        return this;
    };
    this.msg = function (msg, type, callback, closeDelay) {
        this.hidePrevious();
        //
        type = type || -1;
        var config = getMsgConfig();
        //
        config = merge(config, {
            icon: type
        });
        if(typeof callback == "function") {
            config.end = callback;
        }
        //
        layerIndex = layer.msg(msg, config);
        //
        roundTheLayer(20);
        //
        if(closeDelay !== false) {
            closeDelay = closeDelay || 3000;
            closeTimer = setTimeout(function () {
                THIS.hidePrevious();
            }, closeDelay);
        }
        //
        return this;
    };
    this.html = function (html) {
        this.hidePrevious();
        //
        var config = getHtmlConfig();
        //
        config = merge(config, {
            content: html
        });
        //
        layerIndex = layer.open(config);
        //
        return this;
    };
    this.tips = function (html, follow, config) {
        this.hidePrevious();
        //
        config = config || {};
        var theConfig = merge(getTipsConfig(), config);
        //
        follow = filterId(follow);
        layerIndex = layer.tips(html, follow, theConfig);
        //
        return this;
    };
    this.dialog = function (config) {
        this.hidePrevious();
        //
        config = config || {};
        config.src = config.src || "";
        config.dom = config.dom || "";
        config.closeBtn = config.closeBtn ? 1 : false;
        //
        if(config.src) {
            config.content = config.src;
            delete config.src;
            //
            var dlgConfig = merge(getFrameConfig(), config);
            //
            layerIndex = layer.open(dlgConfig);
            //
        } else {
            config.content = config.dom || "";
            delete config.dom;
            if(config.content.charAt(0) == '#') {
                config.content = $(config.content);
            }
            //
            var dlgConfig = merge(getDialogConfig(), config);
            //
            layerIndex = layer.open(dlgConfig);
        }
        //
        return this;
    };

    //返回iframe窗口对象
    this.getFrameWindow = function (theIndex) {
        theIndex = theIndex || layerIndex;
        var theMain = $id("layui-layer" + theIndex).find("> div.layui-layer-content");
        var frames = theMain.find(" > iframe ");
        var frame = frames.size() > 0 ? frames.get(0) : null;
        return frame == null ? null : frame.contentWindow;
    };

    //
    return this;
};
//
Layer.hideAll = function () {
    layer.closeAll();
};
//
Layer.hideTips = function () {
    layer.closeAll("tips");
};
//
Layer.newOne = function () {
    return new Layer.core.fn();
};
Layer.info = function (msg, callback) {
    return Layer.newOne().info(msg, callback);
};
Layer.warning = function (msg, callback) {
    return Layer.newOne().warning(msg, callback);
};
Layer.error = function (msg, callback) {
    return Layer.newOne().error(msg, callback);
};
Layer.progress = function (msg) {
    return Layer.newOne().progress(msg);
};
Layer.loading = function () {
    return Layer.newOne().loading();
};
Layer.confirm = function (msg, yesCallback, noCallback) {
    return Layer.newOne().confirm(msg, yesCallback, noCallback);
};
Layer.prompt = function (config, yesCallback) {
    return Layer.newOne().prompt(config, yesCallback);
};
Layer.msg = function (msg, type, callback, closeDelay) {
    return Layer.newOne().msg(msg, type, callback, closeDelay);
};
Layer.msgInfo = function (msg, callback, closeDelay) {
    return Layer.newOne().msg(msg, "info", callback, closeDelay);
};
Layer.msgWarning = function (msg, callback) {
    return Layer.newOne().msg(msg, "warning", callback, false);
};
Layer.msgSuccess = function (msg, callback, closeDelay) {
    return Layer.newOne().msg(msg, "success", callback, closeDelay);
};
Layer.html = function (html) {
    return Layer.newOne().html(html);
};
Layer.tips = function (html, follow, config) {
    return Layer.newOne().tips(html, follow, config);
};
Layer.dialog = function (config) {
    return Layer.newOne().dialog(config);
};
