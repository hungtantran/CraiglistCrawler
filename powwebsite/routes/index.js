var express = require('express');
var router = express.Router();

var globals = require('./globals');
var rawHTMLProvider = globals.rawHTMLProvider;
var pricesProvider = globals.pricesProvider;

/* GET home page. */
router.get('/', function(req, res) {
  res.render('index', {
    title: 'Weed Price Index',
    markers: globals.locations,
    prices: globals.prices
  });
});

module.exports = router;
