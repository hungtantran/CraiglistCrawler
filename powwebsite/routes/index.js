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

module.exports = router;
