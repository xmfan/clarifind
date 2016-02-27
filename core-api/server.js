'use strict';

var config = require('./config.js'),
    express = require('express'),
    app = express(),
    request = require('request'),
    curl = require('node-curl');
    

app.get('/', function(req, res) {
    var options = {
        url: 'https://api.clarifai.com/v1/color/',
        headers: {
            'Authorization': 'Bearer ' + global.access_token
        }
    };

    function callback(error, response, body) {
        if (error) console.log(error);
        else if (response.statusCode == 401) {
            console.log(body);
            config.requestAccessToken;
            request(options, callback);
        } else {
            res.send(body);
        }
    }
    request(options, callback);
});

app.post('/tag', function(req, res) {
    var options = {
        url: 'https://api.clarifai.com/v1/tag',
        headers: {
            'Authorization': 'Bearer ' + global.access_token
        }, 
        formData: {
            url: ['http://www.clarifai.com/img/metro-north.jpg', 'http://www.clarifai.com/img/metro-north.jpg']
        }
    };

    function callback(error, response, body) {
        if (error) console.log(error);
        else if (response.statusCode == 401) {
            res.sendStatus(body);
            //config.requestAccessToken;
            //request(options, callback);
        } else {
            res.send(JSON.parse(body));
        }
    }
    
    request.post(options, callback);
});

app.get('/android', function(req, res) {
    console.log('android endpoint');
    res.send('android endpoint reached');
});

app.listen(4000, function() {
    console.log('Server running on: 4000');
});
