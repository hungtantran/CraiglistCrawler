var express = require('express');
var router = express.Router();

/* GET privacy page. */
router.get('/', function(req, res) {
  res.render('privacy', {
    title: 'Privacy - Weed Price Index',
    stylesheet: '/stylesheets/index.css'
  });
});

module.exports = router;