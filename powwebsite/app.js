var express = require('express');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var stormpath = require('stormpath')

var index = require('./routes/index');
var prices = require('./routes/prices');
var postings = require('./routes/postings');
var localBusinesses = require('./routes/localBusinesses');
var posting = require('./routes/posting');
var search = require('./routes/search');
var purchase = require('./routes/purchase');
var sale = require('./routes/sale');
var strain = require('./routes/strain');
var type = require('./routes/type');
var news = require('./routes/news');
var user = require('./routes/user')

var app = express();
var https = require('https');
var http = require('http');

// gzip/deflate outgoing responses
var compression = require('compression');
app.use(compression());

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// Redirect 301 to www version only if it's not dev environment
if (app.get('env') !== 'dev') {
    app.get('/*', function(req, res, next) {
        if(/^www\./.test(req.headers.host)) {
            next();
        } else {
            res.redirect(req.protocol+'://www.'+req.headers.host+req.url,301);
        }
    });
}

app.use(favicon());
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', index);
app.use('/search', search);
app.use('/postings', postings);
app.use('/localBusinesses', localBusinesses);
app.use('/prices', prices);
app.use('/posting', posting);
app.use('/purchase', purchase);
app.use('/sale', sale);
app.use('/type', type);
app.use('/strain', strain);
app.use('/news', news);
app.use('/user', user);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

/// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'dev') {
    app.use(function(err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
    
    // app.use(function(err, req, res, next) {
    //     res.statusCode = 302;
    //     res.setHeader('Location', '/');
    //     res.end();
    // });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });

    // res.statusCode = 302;
    // res.setHeader('Location', '/');
    // res.end();
});

/* var fs         = require("fs");
var key_file   = "certificate\\private-key.pem";
var cert_file  = "certificate\\www_leafyexchange_com.crt";
var passphrase = "420pontius";
var config     = {
  key: fs.readFileSync(key_file),
  cert: fs.readFileSync(cert_file)
};

if(passphrase) {
  config.passphrase = passphrase;

https.createServer(config, app).listen(8080);
}*/


http.createServer(app).listen(3000);
