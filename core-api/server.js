'use strict';

var config = require('./config.js'),
    express = require('express'),
    app = express(),
    request = require('request'),
    Firebase = require('firebase');

var ref = new Firebase('https://clarifind.firebaseio.com/');

/*
ref.on("child_added", function(snapshot, prevChildKey) {
    var newPost = snapshot.val();
    console.log(newPost);
});
*/

function pushImage(url) {
    ref.push({
        url: url,
        tags: ['train'],
        address: '200 University Ave, Waterloo'
    });
} 

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

function tagUrl(string) {
    var options = {
        url: 'https://api.clarifai.com/v1/tag',
        headers: {
            'Authorization': 'Bearer ' + global.access_token
        }, 
        formData: {
            url: 'http://www.clarifai.com/img/metro-north.jpg'
        }
    };

    function callback(error, response, body) {
        if (error) console.log(error);
        else if (response.statusCode == 401) {
            console.log(body);
            //config.requestAccessToken;
            //request(options, callback);
        } else {
            pushImage(string);
            return JSON.parse(body);
        }
    }
    
    request.post(options, callback);
};

app.post('/android', function(req, res) {
    console.log(req.data);
    tagUrl(req.data);
    res.send('android endpoint reached');
});

app.get('/getImages', function(req, res) {
    ref.once('value', function(snapshot) {
        var images = [];
        var data = snapshot.val();
        for (var key in data) {
            images.push(data[key]);
        }
        res.send(images);
    }, function (errorObject) {
        console.log('The read failed: ' + errorObject.code);
        res.send(null);
    });
});

function getAllImages() {
    ref.once("value", function(snapshot) {
        var images = [];
        var data = snapshot.val();
        for (var key in data) {
            images.push(data[key]);
        }
        return images;
    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
        return null;
    });
}

app.listen(4000, function() {
    console.log('Server running on: 4000');
});
