var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();

var locations = null;
rawHTMLProvider.getLocations(function(error, docs) {
    if (error != null) {
        console.error('error to get locations: ' + error.stack);
        process.exit(1);
    }

    locations = docs;
    exports.locations = locations;
})

exports.rawHTMLProvider = rawHTMLProvider;

var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();

var prices = null;
pricesProvider.getPrices(function(error, docs) {
    if (error != null) {
        console.error('error to get prices: ' + error.stack);
        process.exit(1);
    }

    prices = docs;
    exports.prices = prices;
})

exports.pricesProvider = pricesProvider;