
function refresh(readonlyfiels)
{
    $.ajax({

        type: "POST",
        url: "cacheData?action=" + (!!readonlyfiels ? 'getreadonly' : 'all'),
        cache: false,
        success: function(json){
            var elem;
            for(var key in json){
                elem = $('#' + key);
                if (elem.attr('type') === 'checkbox'){
                    elem.prop('checked', json[key]);
                }else{
                    elem.val(json[key]);
                }
            }
        }
    });
}

function save()
{
    var json = {};
    $('input:not([readonly])').each(
        function () {
            if (this.type === 'checkbox'){
                json[this.id] = this.checked;
            }else {
                json[this.id] = this.value;
            }
        }
    );
    $.ajax({
        type: "POST",
        url: "cacheData?action=save",
        cache: false,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data:JSON.stringify(json)
    });
}

$(document).ready(function(){
    refresh(false);
    setInterval('refresh(true)',1000);
});