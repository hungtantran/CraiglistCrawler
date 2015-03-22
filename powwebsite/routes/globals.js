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

    postings = docs;
    exports.postings = postings;
})

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.rawHTMLProvider = rawHTMLProvider;

