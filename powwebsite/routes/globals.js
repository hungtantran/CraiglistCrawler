var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();

var locations = null;
postingLocationProvider.getLocations(function(error, docs) {
    if (error != null) {
        console.error('error to get locations: ' + error.stack);
        process.exit(1);
    }

    locations = docs;
    exports.locations = locations;
})

var prices = null;
pricesProvider.getPrices(function(error, docs) {
    if (error != null) {
        console.error('error to get prices: ' + error.stack);
        process.exit(1);
    }

    prices = docs;
    exports.prices = prices;
})

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
