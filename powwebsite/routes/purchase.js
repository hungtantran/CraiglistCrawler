var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();

var MySQLConnectionProvider  = require('./mysqlConnectionProvider.js').MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

// Santity check purchase order request
function sanityCheckRequest(request) {
  if (request === null || request === undefined) {
    return false;
  }

  if (!('lowPrice' in request) ||
    !('highPrice' in request) ||
    !('quantity' in request) ||
    !('zipcode' in request) ||
    !('deliveryDate' in request) ||
    !('zipcode' in request)) {
    return false;
  }

  if (!globals.commonHelper.IsIntValue(request['lowPrice'])) {
    console.log('Request low price invalid');
    return false;
  }

  if (!globals.commonHelper.IsIntValue(request['highPrice'])) {
    console.log('Request high price invalid');
    return false;
  }

  if (!globals.commonHelper.IsIntValue(request['quantity'])) {
    console.log('Request quantity invalid');
    return false;
  }

  if (!globals.commonHelper.IsIntValue(request['zipcode'])) {
    console.log('Request zipcode invalid');
    return false;
  }

  if (!globals.commonHelper.IsIntValue(request['lowPrice'])) {
    console.log('Request low price invalid');
    return false;
  }
  
  var highPrice = parseInt(request['highPrice']);
  var lowPrice = parseInt(request['lowPrice']);

  if (highPrice < lowPrice) {
    console.log('Request high price ' + highPrice + ' smaller than low price' + lowPrice);
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
      var purchaseQuery = 'INSERT INTO purchase_orders (email, lowPrice, highPrice, quantity, deliveryDate, requestDate, deliveryLocation) VALUES (?, ?, ?, ?, ?, CURDATE(), ?)';

      var connection = connectionProvider.getConnection();
      var insertPurchaseOrder = connection.query(purchaseQuery,
        [req.body['email'],
        req.body['lowPrice'],
        req.body['highPrice'],
        req.body['quantity'],
        req.body['deliveryDate'],
        req.body['zipcode']],
        function(err, rows) {
          if (err) {
            console.log('Request to create purchase order failed ' + err);
            responseJson['result'] = false;
            responseJson['message'] = 'Request to create purchase order failed';
          }
      });

      console.log(insertPurchaseOrder.sql);

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