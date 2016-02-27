'use strict';

var config = require('./config.js'),
    express = require('express'),
    app = express(),
    request = require('request'),
    access_token;

requestAccessToken();

app.get('/', function(req, res) {
    var options = {
      url: 'https://api.clarifai.com/v1/color/',
      headers: {
        'Authorization': 'Bearer ' + config.access_token
      }
    };
    console.log(access_token);
    function callback(error, response, body) {
        if (error) console.log(error);
        else if (response.statusCode == 401) {
            config.requestAccessToken();
        }
        request(options, callback);
    }
});


app.listen(4000, function() {
    console.log('Server running on: 4000');
});

function requestAccessToken() {
    request.post({url:'https://api.clarifai.com/v1/token/', form: config.header}, function(error, response, body){
        if (error) console.log(error);
        else access_token = JSON.parse(body).access_token;
        console.log(config.header);
    });
}