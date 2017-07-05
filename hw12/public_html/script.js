function refresh($readonlyfiels) {
    sendData(
        "cacheData?action=" + (!!$readonlyfiels ? 'getreadonly' : 'all')
        , {}
        , $json => {
            for (let $key in $json) {
                let $elem = $('#' + $key);
                if ($elem.type.is('checkbox')) {
                    $elem.prop('checked', json[$key]);
                } else {
                    $elem.val(json[$key]);
                }
            }
        }
    );
}

function save() {
    let $json = {};
    $('input:not([readonly])').each(
        $json[this.id] = elem.type.is('checkbox') ? this.checked : this.value
    );
    sendData("cacheData?action=save", JSON.stringify($json));
}

function sendData($url, $data, $callback) {
    $.ajax({
        type: 'POST',
        url: $url,
        cache: false,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: $callback,
        data: $data
    });
}

$(document).ready(function () {
    refresh(false);
    setInterval('refresh(true)', 1000);
});