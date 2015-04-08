var express = require('express');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var index = require('./routes/index');
var prices = require('./routes/prices');
var postings = require('./routes/postings');
var privacy = require('./routes/privacy');
var terms = require('./routes/terms');
var aboutus = require('./routes/aboutus');
var posting = require('./routes/posting');

var app = express();

// gzip/deflate outgoing responses
var compression = require('compression');
app.use(compression());

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(favicon());
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', index);
app.use('/postings', postings)
app.use('/prices', prices)
app.use('/privacy', privacy)
app.use('/terms', terms)
app.use('/aboutus', aboutus)
app.use('/posting', posting);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

/// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    // app.use(function(err, req, res, next) {
    //     res.status(err.status || 500);
    //     res.render('error', {
    //         message: err.message,
    //         error: err
    //     });
    // });
    app.use(function(err, req, res, next) {
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
    // res.status(err.status || 500);
    // res.render('error', {
    //     message: err.message,
    //     error: {}
    // });
    res.statusCode = 302;
    res.setHeader('Location', '/');
    res.end();
});

app.listen(3000);
