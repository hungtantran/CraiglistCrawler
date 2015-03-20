var express = require('express');
var router = express.Router();

/* GET terms page. */
router.get('/', function(req, res) {
  res.render('terms', {
    title: 'Terms - Weed Price Index',
    stylesheet: '/stylesheets/index.css'
  });
});

module.exports = router;