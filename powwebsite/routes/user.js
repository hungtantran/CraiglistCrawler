var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();

var MySQLConnectionProvider  = require('./mysqlConnectionProvider.js').MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

// Santity check purchase order request
function sanityCheckRequest(request) {
  if (request === null || request === undefined) {
    console.log("undefined");
    return false;
  }

  if (!('email' in request) ||
    !('username' in request) ||
    !('password' in request) ||
    !('retypedPassword' in request)) {
    console.log("missing fields");
    console.log(request);
    return false;
  }

  if (request['password'] != request['retypedPassword']) {
    console.log('Passwords do not match');
    return false;
  }

  return true;
}

router.post('/', function(req, res) {
    var responseJson = {};

    // Sanity check
    if (!sanityCheckRequest(req.body))
    {
      console.log('Request format invalid');
      responseJson['result'] = false;
      responseJson['message'] = 'Request format invalid';
    } else {
      var userQuery = 'INSERT INTO users (email, username, password) VALUES (?, ?, ?)';

      var connection = connectionProvider.getConnection();
      var insertUser = connection.query(userQuery,
        [req.body['email'],
        req.body['username'],
        req.body['password']],
        function(err, rows) {
          if (err) {
            console.log('Request to create user failed ' + err);
            responseJson['result'] = false;
            responseJson['message'] = 'Request to user failed';
          }
      });

      console.log(insertUser.sql);

      res.statusCode = 302;
      res.setHeader('Location', '/');
      res.end();
    }
});

router.get('/', function(req, res) {
      res.render('purchase', {
      title: "We've reached out to sellers in your area!",
      stylesheet: '/stylesheets/index.css',
      description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area!',
      keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
      icon: '/images/icon.png',
      javascriptSrcs: 
          ['http://maps.googleapis.com/maps/api/js',
           'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js',
           'http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js',
           'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
           '/javascripts/index.js']});
      res.end();
});

module.exports = router;