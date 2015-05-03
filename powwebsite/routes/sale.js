var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();

var MySQLConnectionProvider  = require('./mysqlConnectionProvider.js').MySQLConnectionProvider;
var connectionProvider = new MySQLConnectionProvider();

router.get('/:messageId', function(req, res) {
    var messageId = req.params.messageId;

    console.log("Receive sale for message id " + messageId);

    var messageSelect = 'SELECT * FROM message WHERE messageHash = ?';

    var connection = connectionProvider.getConnection();
    var messageQuery = connection.query(messageSelect,
        [messageId],
        function(err, rows) {
          if (err) {
            res.statusCode = 404;
            res.setHeader('Location','/');
            res.end();
        } else {
            if (rows.length == 0) {
                res.render('sale', {
                    title: 'LeafyExchange: The Best Marijuana Prices and Information in the US',
                    stylesheet: '/stylesheets/index.css',
                    description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot',
                    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
                    icon: '/images/leafyexchange.jpg'
                });
            } else {
                messageContent = '\n\n\n______________________\nFrom:\nTo:\nSubject:\n\n' + rows[0]['messageBody'];

                res.render('sale', {
                    title: 'LeafyExchange: The Best Marijuana Prices and Information in the US',
                    stylesheet: '/stylesheets/index.css',
                    description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot',
                    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
                    icon: '/images/leafyexchange.jpg',
                    messageContent: messageContent,
                    messageId: messageId
                });
            }
        }
    });

    connection.end();
    console.log(messageQuery.sql);
});

router.post('/', function(req, res) {
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
    connection.end();
});

module.exports = router;