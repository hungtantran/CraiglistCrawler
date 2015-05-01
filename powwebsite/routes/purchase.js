var express         = require('express');
var format          = require('string-format');
var globals         = require('./globals');
var router          = express.Router();
var nodemailer      = require('nodemailer');

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

var MySQLConnectionProvider  = require("./mysqlConnectionProvider.js").MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

format.extend(String.prototype);


router.post('/', function(req, res) {
    console.log(req.body);

    var purchaseQuery = "INSERT INTO purchase_orders (email, lowPrice, highPrice, quantity, deliveryDate, requestDate, deliveryLocation)" +
      "VALUE (\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",{5},\"{6}\");".format(
        req.body['email'],
        req.body['lowPrice'],
        req.body['highPrice'],
        req.body['quantity'],
        req.body['deliveryDate'],
        "CURDATE()",
        req.body['zipcode']);

    var connection = connectionProvider.getConnection();
    connection.query(purchaseQuery, function(err, rows) {
      if (err) {
        console.log(err);

        res.statusCode = 302;
            res.setHeader('Purchase', '/');
            res.end();
            return;
      } else {
        geocoder.geocode(req.body['zipcode'], function(err, res) {
          getSellers(req.body['email'], rows['insertId'], res[0]['latitude'], res[0]['longitude'], req.body['lowPrice'], req.body['highPrice']);
        });
      }
    });
    console.log("purchase query completed");
});

function getSellers(buyerEmail, purchaseOrderId, purchaseLatitude, purchaseLongitude, lowPrice, highPrice) {
  var sellersQuery =
    "SELECT \
      `subquery`.`price` / `subquery`.`quantity (grams)` AS `price (grams)`, \
      `state`, \
      `city`, \
      `email`, \
      `latitude`, \
      `longitude`, \
      `location_fk`, \
      `location_link_fk`, \
      `datePosted`, \
      `distance`, \
      `timePosted`, \
      `duplicatePostId`, \
      `url`, \
      `active`, \
      `email`, \
      `price_id`, \
      `price_fk`, \
      `price`, \
      `quantity`, \
      `unit`, \
      `quantity (grams)` \
    FROM \
      (SELECT `posting_location`.`state` AS `state`, \
        `posting_location`.`city` AS `city`, \
        `posting_location`.`latitude` AS `latitude`, \
        `posting_location`.`longitude` AS `longitude`, \
        `posting_location`.`location_fk` AS `location_fk`, \
        `posting_location`.`location_link_fk` AS `location_link_fk`, \
        `posting_location`.`datePosted` AS `datePosted`, \
        `posting_location`.`timePosted` AS `timePosted`, \
        `posting_location`.`duplicatePostId` AS `duplicatePostId`, \
        `posting_location`.`url` AS `url`, \
        `posting_location`.`active` AS `active`, \
        `posting_location`.`email` AS `email`, \
        `prices`.`price_id` AS `price_id`, \
        `prices`.`price_fk` AS `price_fk`, \
        `prices`.`price` AS `price`, \
        `prices`.`quantity` AS `quantity`, \
        `prices`.`unit` AS `unit`, \
        CASE WHEN `unit`='oz' THEN `quantity`*28.3495 ELSE `quantity` END AS `quantity (grams)`, \
        SQRT( POW(`latitude`-({0}),2) + POW(`longitude`-({1}),2) ) AS `distance` \
      FROM `posting_location` \
        INNER JOIN `prices` ON (`posting_location`.`location_fk` = `prices`.`price_fk`) \
      WHERE `active`=1 AND `latitude` IS NOT NULL AND `longitude` IS NOT NULL AND `datePosted` IS NOT NULL) AS subquery \
    WHERE `subquery`.`price` / `subquery`.`quantity (grams)` < {2} \
    AND `subquery`.`price` / `subquery`.`quantity (grams)` > {3} \
    ORDER BY `datePosted` DESC, `distance` \
    LIMIT 10".format(purchaseLatitude, purchaseLongitude, highPrice, lowPrice);
  console.log(highPrice);
  console.log(lowPrice);
  console.log(sellersQuery);

  var connection = connectionProvider.getConnection();
  connection.query(sellersQuery, function(err, rows) {
    if (err) {
      console.log(err);

      res.statusCode = 302;
            res.setHeader('Purchase', '/');
            res.end();
            return;
    } else {
      // console.log(rows);
      createSellerOrders(buyerEmail, rows, purchaseOrderId)
    }
  });
}

function createSellerOrders(buyerEmail, sellers, purchaseOrderId) {
  for (var i=0; i</*sellers.length*/1; ++i) {
    var email = "leafyexchange@gmail.com";
    if (sellers[i]['email'] != null) {
      email = sellers[i]['email'];
    }

    var saleQuery = "INSERT INTO sale_orders (purchaseOrderId, postingId, email)" +
      " VALUE (\"{0}\",{1},\"{2}\");".format(
        purchaseOrderId.toString(),
        sellers[i]['price_fk'],
        email);

    var connection = connectionProvider.getConnection();
    connection.query(saleQuery, function(err, rows) {
      console.log(saleQuery);
      if (err) {
        console.log(err);

        res.statusCode = 302;
              res.setHeader('Purchase', '/');
              res.end();
              return;
      } else {
        console.log(rows);
        var saleOrderId = rows['insertId'];
        sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, email);
      }
    });
  }
}

function sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, sellerEmail) {
  var messageBody = "Hi I am interested in your posting!";

  smtpTransport.sendMail({
   from: "Leafy Exchange <leafyexchange@gmail.com>", // sender address
   to: "hungtantran@gmail.com>", // comma separated list of receivers
   subject: "Someone is interested in your posting!", // Subject line
   text: messageBody // plaintext body
  }, function(error, response){
    if (error) {
      console.log(error);
    } else {
      console.log("Message sent:" + response.message);

      // log in the database for 
      var messageQuery = "INSERT INTO message (purchaseOrderId, saleOrderId, messageBody, fromEmail, toEmail, datetime, messageHash)" +
      "VALUE (\"{0}\",\"{1}\",\"{2}\",\"{3}\",\"{4}\",{5},\"{6}\");".format(
        purchaseOrderId,
        saleOrderId,
        messageBody,
        buyerEmail,
        sellerEmail,
        "CURDATE()",
        purchaseOrderId.toString() + saleOrderId.toString());

      console.log(messageQuery);
    }
  });

}

function createMessageRows() {

}

module.exports = router;