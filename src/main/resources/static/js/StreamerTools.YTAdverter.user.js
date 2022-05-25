// ==UserScript==
// @name         StreamerTools.YTAdverter
// @namespace    https://youtube.com/watch
// @version      0.3
// @description  Advert now playing from Youtube to StreamerTools Service
// @author       Diatorker
// @match        https://www.tampermonkey.net/index.php?version=4.13&ext=dhdg&updated=true
// @icon         https://www.google.com/s2/favicons?domain=tampermonkey.net
// @grant        none
// @include      https://www.youtube.com/*
// @include      https://youtube.com/*
// @require      https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.2/sockjs.min.js
// @require      https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js
// ==/UserScript==

var stompClient = null;
var socket = null;
var uuid = self.crypto.randomUUID();

(function() {
    'use strict';

    console.log('YTAdverter - Tab UUID: ' + uuid);

    connect();

    var intervalId = setInterval(refreshNP, 1000);

})();

function connect() {
    socket = new SockJS('https:/localhost:8443/youtube');
    stompClient = Stomp.over(socket);
    stompClient.debug = function(str) {};
    stompClient.connect({}, function (frame) {
        console.log('YTAdverter - Connected: ' + frame);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("YTAdverter - Disconnected");
}

function refreshNP() {
    if(stompClient !== null && stompClient.connected){
        let urlParams = new URLSearchParams(window.location.search);
        stompClient.send("/app/youtube/now-watching", {}, JSON.stringify({'video': urlParams.get('v'), 'adverter': uuid}));
        console.log("YTAdverter - Adverting video id :" + urlParams.get('v'));
    }
    else {
        connect()
    }
}