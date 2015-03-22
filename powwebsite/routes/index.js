var express = require('express');
var router = express.Router();

var globals = require('./globals');

/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', {
    title: 'Weed Price Index',
    stylesheet: '/stylesheets/index.css',
    markers: globals.locations,
    postings: globals.postings
  });
});

// Get some state
router.get('/state/:state', function(req, res){
    var state = req.params.state

    stateInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
            stateInfo.push(globals.postings[i]);
        }
    }

    res.render('index', {
    title: 'Weed Price Index in ' + state,
    stylesheet: '/stylesheets/index.css',
    markers: stateInfo,
    postings: stateInfo
  });
});

module.exports = router;
