ws = (() => {
    let path = window.location.href;
    let webSocket = new WebSocket('ws:' + path.substring(path.indexOf("//")) + 'cacheAdmin');
    webSocket.onopen = (message) => console.log('webSocket opened:' + message);
    webSocket.onmessage = (message) => {
        console.log('webSocket message:' + message.data);
        let resp = JSON.parse(message.data);
        for (let key in resp) {
            let elem = document.getElementById(key);
            if (elem.type === 'checkbox') {
                elem.checked = resp[key];
            } else {
                elem.value = resp[key];
            }
        }
    };
    webSocket.onclose = (message) => console.log('webSocket closed:' + message);
    webSocket.onerror = (message) => console.log('webSocket error:' + message);

    return {
        save: () => {
            let data = {};
            let elements = document.querySelectorAll('input:not([readonly])');
            elements.forEach(elem => {
                data[elem.id] = elem.type === 'checkbox' ? elem.checked : elem.value
            });
            console.log('sendData:' + JSON.stringify(data));
            webSocket.send(JSON.stringify(data));
        }
    }
})();