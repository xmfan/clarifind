'use strict';

var config = require('./config.js'),
    express = require('express'),
    app = express(),
    request = require('request');

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


app.listen(4000, function() {
    console.log('Server running on: 4000');
});