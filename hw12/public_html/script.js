
function refresh()
{
    $.ajax({
        url: "cacheData",
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
$(document).ready(function(){
    refresh();
    setInterval('refresh()',1000);
});