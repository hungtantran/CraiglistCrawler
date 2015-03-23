var express = require('express');
var router = express.Router();

var globals         = require('./globals');

/* GET contact us page. */
router.get('/', function(req, res) {
  res.render('contactus', {
    title: 'Contact Us - Weed Price Index',
    stylesheet: '/stylesheets/index.css',
    states: globals.states
  });
});

module.exports = router;