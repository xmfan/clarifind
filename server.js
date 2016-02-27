'use strict';

var express = require('express'),
    app = express();

app.get('/', function(req, res) {
    res.send('hi');
});

app.listen(4000, function() {
    console.log('Server running on: 4000');
});