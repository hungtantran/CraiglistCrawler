var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();
var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();
var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();

var postings = [];
var prices = [];
var postingLocations = [];

function UpdatePostingCache(error, docs) {
    if (error != null) {
        console.error('error to get postings: ' + error.stack);
        process.exit(1);
    }

    postings = [];
    var idSets = new Array();

    var index = 0;
    for (var i = 0; i < docs.length; ++i)
    {
        var duplicatedId = docs[i]['duplicatePostId'];
        if (duplicatedId !== null) {
            if (idSets.indexOf(duplicatedId) != -1) {
                continue;
            } else {
                idSets.push(duplicatedId);
            }
        } else {
            if (idSets.indexOf(docs[i]['id']) != -1) {
                continue;
            }
        }

        postings[index] = {};

        postings[index]['id'] = docs[i]['id'];
        postings[index]['price'] = commonHelper.ParsePrices(docs[i]['price']);
        postings[index]['quantity'] = commonHelper.ParseQuantities(docs[i]['quantity']);
        postings[index]['state'] = docs[i]['state'];
        postings[index]['city'] = docs[i]['city'];
        postings[index]['datePosted'] = docs[i]['datePosted'];
        postings[index]['title'] = docs[i]['title'];

        postings[index]['url'] = commonHelper.ReplaceAll(' ', '-', postings[index]['title']);
        postings[index]['url'] += "-" + postings[index]['id'];

        postings[index]['lat'] = docs[i]['lat1'];
        postings[index]['lng'] = docs[i]['lng1'];

        if (postings[index]['lat'] == null || postings[index]['lng'] == null) {
            postings[index]['lat'] = docs[i]['lat2'];
            postings[index]['lng'] = docs[i]['lng2'];
        }

        ++index;
    }
}

function RefreshCache() {
    pricesProvider.getPostings(function(error, docs) {
        UpdatePostingCache(error, docs);
        console.log("Postings cache has " + postings.length + " entries")
        exports.postings = postings;
    })

    pricesProvider.getAllPrices(function(error, docs) {
        prices = docs;
        console.log("Prices cache has " + prices.length + " entries");
        exports.prices = prices;
    })

    pricesProvider.getAllPostingLocations(function(error, docs) {
        postingLocations = docs;
        console.log("PostingLocations cache has " + postingLocations.length + " entries");
        exports.postingLocations = postingLocations;
    })
}

RefreshCache();

// Periodically refresh cache every 5 minutes
setInterval(function() {
    RefreshCache();
}, 300000);

var states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];
exports.states = states;

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.rawHTMLProvider = rawHTMLProvider;
exports.commonHelper = commonHelper;


