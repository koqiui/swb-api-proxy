if (!Ajax.setted()) {
    // 设置rest api基本url
    if (typeof getRestUrl === 'function' && getRestUrl('') !== null) {
        Ajax.baseUrl(getRestUrl(''));
    } else if (typeof getAppUrl === 'function') {
        // 设置Ajax基本url
        Ajax.baseUrl(getAppUrl(''));
    }

    //
    function ajaxErrorCallback(message) {
        if (typeof Layer !== 'undefined') {
            Layer.error(message);
        } else {
            alert(message);
        }
    }
    //
    Ajax.errorCallback(ajaxErrorCallback);
}
