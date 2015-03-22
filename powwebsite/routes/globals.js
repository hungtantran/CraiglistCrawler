var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();
var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();

var postings = null;
pricesProvider.getPostings(function(error, docs) {
    if (error != null) {
        console.error('error to get postings: ' + error.stack);
        process.exit(1);
    }

    postings = [];

    for (var i = 0; i < docs.length; ++i)
    {
        postings[i] = {};

        postings[i]['id'] = docs[i]['id'];
        postings[i]['price'] = docs[i]['price'];
        postings[i]['quantity'] = docs[i]['quantity'];
        postings[i]['unit'] = docs[i]['unit'];
        postings[i]['state'] = docs[i]['state'];
        postings[i]['city'] = docs[i]['city'];
        postings[i]['datePosted'] = docs[i]['datePosted'];

        postings[i]['lat'] = docs[i]['lat1'];
        postings[i]['lng'] = docs[i]['lng1'];

        if (postings[i]['lat'] == null || postings[i]['lng'] == null) {
            postings[i]['lat'] = docs[i]['lat2'];
            postings[i]['lng'] = docs[i]['lng2'];
        }
    }

    exports.postings = postings;
})

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.rawHTMLProvider = rawHTMLProvider;

