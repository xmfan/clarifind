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
    var picArray = ['http://i.imgur.com/HFNWMXo.jpg','http://www.freelargeimages.com/wp-content/uploads/2015/11/Wedding_Rings_06.png','https://upload.wikimedia.org/wikipedia/commons/b/b7/Unico_Anello.png','http://www.ablogtowatch.com/wp-content/uploads/2009/10/tissot-v8-quartz-chronograph-2009-watch.jpg','http://static.highsnobiety.com/wp-content/uploads/selectism/2012/07/Buyers-guide-watches-6.jpg','http://ep.yimg.com/ay/laf-store/women-s-livestrong-baseball-cap-black-14.jpg','http://www.alvinccstore.com/outerweb/product_images/Baseball_CapL.jpg','http://i.imgur.com/F5R5Zb1.jpg','http://i.imgur.com/md47wga.jpg','http://i.imgur.com/rnMUkal.jpg','http://i.imgur.com/Ooxto0k.jpg','http://i.imgur.com/TKs7hKm.jpg','http://i.imgur.com/QqxkofT.jpg','http://i.imgur.com/f5dD83m.jpg','http://i.imgur.com/iEYoSi4.jpg','http://i.imgur.com/RUGylai.jpg','http://i.imgur.com/zcu0lFF.jpg','http://i.imgur.com/AQF2Zvh.jpg','http://i.imgur.com/xTcMMGw.jpg','http://i.imgur.com/4ocA7Yl.jpg','http://i.imgur.com/P63Kfw8.jpg','http://i.imgur.com/dPD0jKa.jpg','http://i.imgur.com/jYotBON.jpg','http://i.imgur.com/cS5v6Ec.jpg','http://i.imgur.com/5Z3KKnc.jpg','http://i.imgur.com/XDzYaXH.jpg','http://i.imgur.com/CVeeNJT.jpg','http://i.imgur.com/cGD3ZO0.jpg','http://i.imgur.com/QxrdcfX.jpg','http://i.imgur.com/xtwVSQV.jpg','http://i.imgur.com/HArQ1uh.jpg','http://i.imgur.com/nw5AP6g.jpg','http://i.imgur.com/Eztqs0l.jpg','http://i.imgur.com/NvkxNJ3.jpg','http://i.imgur.com/mjzkHhR.jpg','http://i.imgur.com/CvUItgC.jpg','http://i.imgur.com/mjwKo7p.jpg','http://i.imgur.com/rMlBuoa.jpg','http://i.imgur.com/EcRcupM.jpg','http://i.imgur.com/Qtx63}7.jpg','http://i.imgur.com/p0opGTJ.jpg','http://i.imgur.com/tHPxfEb.jpg'];
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
