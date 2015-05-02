var nodemailer      = require('nodemailer');
var crypto          = require('crypto');

var MySQLConnectionProvider  = require('./mysqlConnectionProvider.js').MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

var smtpTransport = nodemailer.createTransport('SMTP', {
  service: 'Gmail',
  auth: {
    user: 'leafyexchange@gmail.com',
    pass: '420pontius'
  }
});

var geocoderProvider = 'google';
var httpAdapter = 'http';
var extra = { /* apiKey: 'what is our API key?' */ }
var geocoder = require('node-geocoder')(geocoderProvider, httpAdapter, extra);

var MAX_PURCHASE_ORDER_PROCESS_EACH_TIME = 1;
var MAX_EMAIL_SENT_PER_PURCHASE_ORDER = 1;
var MIN_PER_PERIODIC_PROCESS = 1;

PeriodicProcess = function() {
};

// Hash message
function hashMessage(message) {
  if (message === null || message === undefined) {
    return null;
  }

  var hashedMessage = crypto.createHash('md5').update(message).digest('hex');
  return hashedMessage;
}

function ProcessPurchaseOrders() {
    console.log("Process purchase order");

    var getPurchaseOrders = 'SELECT * FROM purchase_orders WHERE processed = 0 AND deliveryDate >= CURDATE() ORDER BY deliveryDate ASC, requestDate DESC LIMIT ' + MAX_PURCHASE_ORDER_PROCESS_EACH_TIME;

    var connection = connectionProvider.getConnection();
    var getPurchaseQuery = connection.query(getPurchaseOrders, function(err, rows) {
        if (err) {
            console.log('Fail to query for unprocessed purchase orders ' + err);
            return;
        } else {
            for (var i = 0; i < rows.length; ++i) {
                var order = rows[i];

                geocoder.geocode(order['deliveryLocation'], function(err, res) {
                  if (err) {
                    console.log('Fail to geocode zipcode ' + order['deliveryLocation'] + ' for id ' + order['purchaseOrderId']);
                    // TODO: log some sort of error here
                  } else {
                    var id = order['purchaseOrderId'];
                    getSellers(
                      order['email'],
                      id,
                      res[0]['latitude'],
                      res[0]['longitude'],
                      order['lowPrice'],
                      order['highPrice']);

                    var connection2 = connectionProvider.getConnection();

                    var setProcessedPurchaseOrder = 'UPDATE purchase_orders SET processed = 1 WHERE purchaseOrderId = ' + id + ';';

                    connection2.query(setProcessedPurchaseOrder, function(err, rows) {
                      console.log('Finished processing purchase order ' + id);
                    });

                    connection2.end();
                  }
                });
            }
        }
    });

    connection.end();
}

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
      WHERE `active`=1 AND `latitude` IS NOT NULL AND `longitude` IS NOT NULL AND `datePosted` IS NOT NULL AND email IS NOT NULL AND email <> \'NoEmail\' AND email <> \'Expired\') AS subquery \
    WHERE `subquery`.`price` / `subquery`.`quantity (grams)` < ? \
    AND `subquery`.`price` / `subquery`.`quantity (grams)` > ? \
    ORDER BY `datePosted` DESC, `distance` \
    LIMIT ' + MAX_EMAIL_SENT_PER_PURCHASE_ORDER;

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
          console.log('Fail to insert into message table ' + err);
          connection.end();
          return;
        }
      });

      console.log(insertMessage.sql);
      connection.end();
    }
  });
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
        var saleOrderId = rows['insertId'];
        sendSellerEmails(purchaseOrderId, saleOrderId, buyerEmail, email);
      }
    });

    console.log(insertSaleOrderQuery.sql);
  }

  connection.end();
}

ProcessPurchaseOrders();

// Periodically process purchase order every 1 minute
setInterval(function() {
    ProcessPurchaseOrders();
}, MIN_PER_PERIODIC_PROCESS * 60000);

exports.PeriodicProcess = PeriodicProcess;