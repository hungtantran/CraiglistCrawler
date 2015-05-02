var express         = require('express');
var format          = require('string-format');
var globals         = require('./globals');
var router          = express.Router();
var nodemailer      = require('nodemailer');
var crypto          = require('crypto');

var geocoderProvider = 'google';
var httpAdapter = 'http';
var extra = {
//   apiKey: 'what is our API key?'
}
var geocoder            = require('node-geocoder')(geocoderProvider, httpAdapter, extra);
var smtpTransport = nodemailer.createTransport('SMTP', {
  service: 'Gmail',
  auth: {
    user: 'leafyexchange@gmail.com',
    pass: '420pontius'
  }
});

var MySQLConnectionProvider  = require('./mysqlConnectionProvider.js').MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

format.extend(String.prototype);

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
  
  if (parseInt(request['highPrice']) < parseInt(request['lowPrice'])) {
    console.log('Request high price smaller than low price');
    return false;
  }

  return true;
}

// Hash message
function hashMessage(message) {
  if (message === null || message === undefined) {
    return null;
  }

  var hashedMessage = crypto.createHash('md5').update(message).digest('hex');
  return hashedMessage;
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
          } else {
            responseJson['result'] = true;
            responseJson['message'] = 'Successfully create purchase order';

            // TODO: make this a consumer-producer pattern, processing
            // purchase order in a different thread instead of right here
            geocoder.geocode(req.body['zipcode'], function(err, res) {
              getSellers(
                req.body['email'],
                rows['insertId'],
                res[0]['latitude'],
                res[0]['longitude'],
                req.body['lowPrice'],
                req.body['highPrice']);
            });
          }
      });

      console.log(insertPurchaseOrder.sql);
      connection.end();
    }

    res.statusCode = 302;
    res.setHeader('Location','/');
    res.end();
});

function getSellers(buyerEmail, purchaseOrderId, purchaseLatitude, purchaseLongitude, lowPrice, highPrice) {
  var sellersQuery =
    'SELECT DISTINCT \
      `email`, \
      `price_fk` \
    FROM \
      (SELECT `posting_location`.`latitude` AS `latitude`, \
        `posting_location`.`longitude` AS `longitude`, \
        `posting_location`.`location_fk` AS `location_fk`, \
        `posting_location`.`datePosted` AS `datePosted`, \
        `posting_location`.`active` AS `active`, \
        `posting_location`.`email` AS `email`, \
        `prices`.`price_fk` AS `price_fk`, \
        `prices`.`price` AS `price`, \
        `prices`.`quantity` AS `quantity`, \
        `prices`.`unit` AS `unit`, \
        CASE WHEN `unit`=\'oz\' THEN `quantity`*28.3495 ELSE `quantity` END AS `quantity (grams)`, \
        SQRT( POW(`latitude`-(?),2) + POW(`longitude`-(?),2) ) AS `distance` \
      FROM `posting_location` \
        INNER JOIN `prices` ON (`posting_location`.`location_fk` = `prices`.`price_fk`) \
      WHERE `active`=1 AND `latitude` IS NOT NULL AND `longitude` IS NOT NULL AND `datePosted` IS NOT NULL AND email IS NOT NULL) AS subquery \
    WHERE `subquery`.`price` / `subquery`.`quantity (grams)` < ? \
    AND `subquery`.`price` / `subquery`.`quantity (grams)` > ? \
    ORDER BY `datePosted` DESC, `distance` \
    LIMIT 5';

  var connection = connectionProvider.getConnection();
  var selectSellersQuery = connection.query(sellersQuery, [
    purchaseLatitude,
    purchaseLongitude,
    highPrice,
    lowPrice],
    function(err, rows) {
      if (err) {
        console.log(err);
        /* TODO log error here */
      } else {
        createSellerOrders(buyerEmail, rows, purchaseOrderId)
      }
  });

  console.log(selectSellersQuery.sql);
  connection.end();
}

function createSellerOrders(buyerEmail, sellers, purchaseOrderId) {
  var connection = connectionProvider.getConnection();

  for (var i = 0; i < sellers.length; ++i) {
    var email = 'leafyexchange@gmail.com';

    if (sellers[i]['email'] !== null) {
      email = sellers[i]['email'];
    } else {
      continue;
    }

    var saleQuery = 'INSERT INTO sale_orders (purchaseOrderId, postingId, email) VALUES (?, ?, ?)';

    var insertSaleOrderQuery = connection.query(saleQuery, [
      purchaseOrderId.toString(),
      sellers[i]['price_fk'],
      email],
      function(err, rows) {
      if (err) {
        console.log(err);
        /* TODO log error here */
        connection.end();
        return;
      } else {
        console.log(rows);
        var saleOrderId = rows['insertId'];
        sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, email);
      }
    });

    console.log(insertSaleOrderQuery.sql);
  }

  connection.end();
}

function sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, sellerEmail) {
  if (purchaseOrderId === null || purchaseOrderId === undefined ||
    saleOrderId === null || saleOrderId === undefined ||
    buyerEmail === null || buyerEmail === undefined ||
    sellerEmail === null || sellerEmail === undefined) {
    return;
  }

  var messageBody = 'Hi I am interested in your posting!';
  var hashedMessage = hashMessage(purchaseOrderId + saleOrderId + buyerEmail + sellerEmail + messageBody);

  smtpTransport.sendMail({
   from: 'Leafy Exchange <leafyexchange@gmail.com>', // sender address
   to: 'roger.l.hau@gmail.com, hungtantran@gmail.com', // receivers
   subject: 'Someone is interested in your posting!', // Subject line
   text: messageBody // plaintext body
  }, function(error, response){
    if (error) {
      console.log(error);
      return;
    } else {
      console.log('Message sent:' + response.message);

      // log in the database for 
      var messageQuery = 'INSERT INTO message (purchaseOrderId, saleOrderId, messageBody, fromEmail, toEmail, datetime, messageHash) VALUES (?, ?, ?, ?, ?, NOW(), ?)';

      var connection = connectionProvider.getConnection();
      var insertMessage = connection.query(messageQuery, [
        purchaseOrderId,
        saleOrderId,
        messageBody,
        buyerEmail,
        sellerEmail,
        hashedMessage],
        function(err, rows) {
        if (err) {
          console.log(err);
          connection.end();
          return;
        } else {
          var saleOrderId = rows['insertId'];
          sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, sellerEmail);
        }
      });

      console.log(insertMessage.sql);
      connection.end();
    }
  });
}

function createMessageRows() {
}

module.exports = router;