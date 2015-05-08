var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();
var CommonHelper    = require('./commonHelper').CommonHelper;
var commonHelper    = new CommonHelper();

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
                var messageText = commonHelper.convertMessageBodyToMessageText(rows[0]['messageBody']);
                console.log('text = ' + messageText);
                messageContent = '\n\n\n______________________\n' + messageText;

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
    if (!('messageId' in req.body) ||
        !('reply' in req.body) ||
        !('email' in req.body)) {
        console.log("This shouldn't happen");
        res.statusCode = 404;
        res.setHeader('Location','/');
        res.end();
        return;
    }

    console.log('Receive new message');
    console.log(req.body);
    var messageId = req.body['messageId'];
    var messageSelect = 'SELECT * FROM message WHERE messageHash = ?';

    var connection = connectionProvider.getConnection();
    var messageQuery = connection.query(messageSelect,
        [messageId],
        function(err, rows) {
            if (err) {
                res.statusCode = 404;
                res.setHeader('Location','/');
                res.end();
                return;
            } else {
                console.log('Find ' + rows.length + ' messages with hash ' + messageId);
                if (rows.length == 0) {
                    // This shouldn't happen
                    res.statusCode = 404;
                    res.setHeader('Location','/');
                    res.end();
                    return;
                } else if (rows.length == 1) {
                    // The normal case
                    var message = rows[0];
                    var hashedMessage = commonHelper.HashString(message['purchaseOrderId'] + message['saleOrderId'] + req.body['email'] + message['fromEmail'] + Math.random());
                    // Insert the new message into database to be sent
                    var messageQuery = 'INSERT INTO message (purchaseOrderId, saleOrderId, messageBody, messageHTML, fromEmail, toEmail, datetime, messageHash, replyTo) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?, ?)';

                    var messageBody = "<MessageElem>You got a new message on Leafy Exchange!<MessageElem>{0}<MessageElem>http://www.leafyexchange.com/sale/{1}<MessageElem>".format(req.body['reply'], hashedMessage);

                    var connection = connectionProvider.getConnection();
                    var insertMessage = connection.query(messageQuery, [
                        message['purchaseOrderId'],
                        message['saleOrderId'],
                        messageBody, /* Message body */
                        '', /* Message html */
                        req.body['email'],
                        message['fromEmail'],
                        hashedMessage,
                        message['id']],
                        function(err, rows) {
                        if (err) {
                          console.log('Fail to insert into message table ' + err);
                          connection.end();
                          return;
                        }
                    });

                    console.log(insertMessage.sql);
                    connection.end();

                    res.statusCode = 200;
                    res.setHeader('Location','/');
                    res.end();
                    return;
                } else {
                    // Very unexpected case
                    res.statusCode = 404;
                    res.setHeader('Location','/');
                    res.end();
                    return;
                }
            }
        });
    res.statusCode = 200;
    res.setHeader('Location','www.leafyexchange.com');
    res.end();
});

// Tracking email
router.get('/email/:messageHash', function(req, res) {
    var messageHash = req.params.messageHash;
    console.log("received request for messageHash:" + messageHash);

    var messageViewQuery = 'INSERT INTO message_views (messageHash, viewTimeStamp) \
        VALUES (?, CURRENT_TIMESTAMP())';
    var connection = connectionProvider.getConnection();
    var insertMessageView = connection.query(messageViewQuery,
        [messageHash], function(err, rows) {
            if (err) {
                console.log("Error for receiving view for messageHash " + messageHash);
                responseJson['result'] = false;
                responseJson['message'] = 'Request to create purchase order failed';
            }
        })

    res.json([]);
})
module.exports = router;