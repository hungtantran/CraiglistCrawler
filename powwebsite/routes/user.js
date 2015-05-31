var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();

var globals         = require('./globals');
var userProvider   = globals.userProvider;

var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
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

// Sign Up
router.post('/signup', function(req, res) {
    var responseJson = {};

    // Sanity check
    if (!sanityCheckRequest(req.body))
    {
      console.log('Request format invalid');
      responseJson['result'] = false;
      responseJson['message'] = 'Request format invalid';
    } else {
      userProvider.insertUser(
        req.body['email'],
        req.body['username'],
        req.body['password'],
        function(error, doc) {
          if (error) {
            console.log(error);
          }
        }
      );

      res.statusCode = 302;
      res.setHeader('Location', '/');
      res.end();
    }
});

router.get('/', function(req, res) {
  var userId = req.session.user['id']
  var transactionsQuery = 'SELECT * FROM transactions WHERE buyer_id=? OR seller_id=?';

  var transactions = null;
  var connection = connectionProvider.getConnection();
  var transactionsQueryResults = connection.query(transactionsQuery,
    [userId,
    userId],
    function(err, rows) {
      if (err) {
        console.log('Request for transactions failed ' + err);
        responseJson['result'] = false;
        responseJson['message'] = 'Request for transactions failed';
      } else {
          console.log(rows);

          var purchases = [];
          var sales = [];

          for (var i=0; i<rows.length; i++) {
            var row = rows[i];
            console.log(row);
            if (row['buyer_id']==userId) purchases.push(row);
            if (row['seller_id']==userId) sales.push(row);
          }

          console.log(sales);
          console.log(purchases);

          res.render('user', {
          title: 'The Best Weed Prices and Delivery Source - LeafyExchange',
          stylesheet: '/stylesheets/user.css',
          session: req.session,
          postings: globals.postings,
          purchases: purchases,
          sales: sales,
          localBusinesses: globals.localBusinesses,
          states: globals.states,
          pricesString: globals.commonHelper.constructPriceStringArray(globals.postings),
          quantitiesString: globals.commonHelper.constructQuantityStringArray(globals.postings),
          description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area!',
          keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
          icon: '/images/icon.png',
          javascriptSrcs: 
              ['http://maps.googleapis.com/maps/api/js?libraries=places&sensor=false',
               'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js',
               'http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js',
               'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
               '/javascripts/index.js']
          });
      }
  });
});

// Log In
router.post('/login', function(req, res) {
  if (req.session.logged) {
    // TODO sth 
  } else {
    userProvider.getUser(
      req.body['username'],
      req.body['password'],
      function(error, doc) {
        if (error) {
          console.log(error);
        } else {
          if (doc && doc.length == 1) {
            req.session.logged = true;
            req.session.user = doc[0];
            console.log('log in successfully');
          } else {
            console.log(doc);
            console.log('log in fail');
          }
        }

        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
      }
    );
  }
});

// Log Out
router.post('/logout', function(req, res) {
  if (req.session.logged) {
    req.session.logged = false;
  }

  res.statusCode = 302;
  res.setHeader('Location', '/');
  res.end();
});

router.get('/logout', function(req, res) {
  if (req.session.logged) {
    req.session.logged = false;
  }

  res.statusCode = 302;
  res.setHeader('Location', '/');
  res.end();
});

module.exports = router;