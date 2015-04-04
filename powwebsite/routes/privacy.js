var express = require('express');
var router = express.Router();

var globals         = require('./globals');

/* GET privacy page. */
router.get('/', function(req, res) {
  res.render('privacy', {
    title: 'Privacy - Weed Price Index',
    stylesheet: '/stylesheets/index.css',
    states: globals.states,
    description: 'description',
    keywords: 'keywords',
    icon: 'icon'
  });
});

module.exports = router;