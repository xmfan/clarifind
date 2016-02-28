'use strict';

var config = require('./config.js'),
    express = require('express'),
    app = express(),
    request = require('request'),
    Firebase = require('firebase'),
    bodyParser = require('body-parser');

app.use(bodyParser.json());
app.use(express.static('public'));

var ref = new Firebase('https://clarifind.firebaseio.com/');

/*
ref.on("child_added", function(snapshot, prevChildKey) {
    var newPost = snapshot.val();
    console.log(newPost);
});
*/

function pushImage(url, tags, address) {
    ref.push({
        url: url,
        tags: tags,
        address: address 
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

function tagUrl(picArray, res) {
    var options = {
        url: 'https://api.clarifai.com/v1/tag',
        headers: {
            'Authorization': 'Bearer ' + global.access_token
        }, 
        formData: {
            url: picArray 
        }
    };

    function callback(error, response, body) {
        if (error) console.log(error);
        else if (response.statusCode == 401) {
            console.log(body);
        } else {
            var resp = JSON.parse(body);
            var images = resp.results;
            for (var i=0; i<images.length; i++) {
                pushImage(images[i].url, images[i].result.tag.classes, '200 University Ave');
            }
            res.send(JSON.parse(body));
        }
    }
    
    request.post(options, callback);
};

app.post('/populate', function(req, res) {
    console.log(req.params);
    console.log(req.data);
    var picArray = ["http://i.imgur.com/N3eQ0Ff.jpg", "http://i.imgur.com/7YbEvFr.jpg","http://i.imgur.com/AcL8teC.jpg","http://i.imgur.com/GZDoxNc.jpg","http://i.imgur.com/8eQsB03.png","http://i.imgur.com/IylryVE.jpg","http://i.imgur.com/FH4jIik.jpg","http://i.imgur.com/2tSLIVN.jpg","http://i.imgur.com/cFKQkkE.jpg","http://i.imgur.com/z8TRbXo.jpg","http://i.imgur.com/X9R55DO.jpg","http://i.imgur.com/tZXQFw5.jpg", "http://i.imgur.com/k7HAN6X.jpg"];
    tagUrl(picArray, res);
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

//app.use(express.bodyParser());
