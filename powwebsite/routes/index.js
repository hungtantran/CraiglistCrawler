var express = require('express');
var router = express.Router();

var globals = require('./globals');

/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', {
    title: 'Weed Price Index',
    markers: globals.locations,
  });
});

module.exports = router;
