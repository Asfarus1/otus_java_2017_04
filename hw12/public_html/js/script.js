sendRequest = (() => {
    let sendData = (action, data, callback) =>{
        $.ajax({
            type: 'POST',
            url: 'cacheData?action=' + action,
            cache: false,
            dataType: 'json',
            success: callback,
            data: JSON.stringify(data),
            contentType: 'application/json;charset=utf-8',
        });
    }

    return {
        refresh: (isReadonlyFields) => {
            sendData(
                (isReadonlyFields ? 'getreadonly' : 'all')
                , {}
                , resp => {
                    for (let key in resp) {
                        let elem = $('#' + key)[0];
                        if (elem.type === 'checkbox') {
                            elem.checked = resp[key];
                        } else {
                            elem.value = resp[key];
                        }
                    }
                }
            );
        },

        save: () => {
            let data = {};
            $('input:not([readonly])').each(
                (i, elem) => data[elem.id] = elem.type === 'checkbox' ? elem.checked : elem.value
            );
            sendData('save', data);
        }
    }
})();

$(() => {
    sendRequest.refresh(false);
    setInterval('sendRequest.refresh(true)', 1000);
});