var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();
var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();
var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();

// For ex: [10.0, 4.0, 40.0, 7.0, 60.0, 14.0, 100.0, 1.0, 28.0, 200.0, 4.0, 112.0, 650.0, 253.0, 414.0, 4380.0]
function ParsePrices(prices) {
    if (!prices) {
        return prices;
    }

    var priceArray = commonHelper.ParseArrayString(prices, ',', 3, 1000, true);

    return priceArray;
}

// For ex: [ 0.125  0.25   0.5  ] g[ 0.125  0.25   0.5  ] oz
function ParseQuantities(quantities) {
    if (!quantities) {
        return quantities;
    }

    var quantitiesType = quantities.split('[');

    var quantitiesArray = [];
    quantitiesArray['gram'] = null;
    quantitiesArray['ounce'] = null;

    // One empty, one for gram, one for oz
    if (quantitiesType.length < 2) {
        return quantitiesArray;
    }

    var gramArray = commonHelper.ParseArrayString(quantitiesType[1], ' ', 1.1, 200, true);
    quantitiesArray.push(gramArray);

    // One empty, one for gram, one for oz
    if (quantitiesType.length < 3) {
        return quantitiesArray;
    }
    var ounceArray = commonHelper.ParseArrayString(quantitiesType[2], ' ', 0.1, 200, false);
    quantitiesArray.push(ounceArray);

    return quantitiesArray;
}

var postings = [];
var prices = [];

// function UpdateCache(error, docs) {
//     if (error != null) {
//         console.error('error to get postings: ' + error.stack);
//         process.exit(1);
//     }

//     postings = [];

//     for (var i = 0; i < docs.length; ++i)
//     {
//         postings[index] = {};

//         postings[index]['id'] = docs[i]['id'];
//         postings[index]['price'] = docs[i]['price'];
//         postings[index]['quantity'] = docs[i]['quantity'];
//         postings[index]['unit'] = docs[i]['unit'];
//         postings[index]['state'] = docs[i]['state'];
//         postings[index]['city'] = docs[i]['city'];
//         postings[index]['datePosted'] = docs[i]['datePosted'];
//         postings[index]['title'] = docs[i]['title'];
//         postings[index]['url'] = ReplaceAll(' ', '-', postings[index]['title']);
//         postings[index]['url'] += "-" + postings[index]['id'];

//         postings[index]['lat'] = docs[i]['lat1'];
//         postings[index]['lng'] = docs[i]['lng1'];

//         if (postings[index]['lat'] == null || postings[index]['lng'] == null) {
//             postings[index]['lat'] = docs[i]['lat2'];
//             postings[index]['lng'] = docs[i]['lng2'];
//         }
//     }

//     console.log("Cache has " + postings.length + " entries")
// }

// pricesProvider.getPostings(function(error, docs) {
//     UpdateCache(error, docs);
//     exports.postings = postings;
// })

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
                // console.log("Post " + docs[i]['id'] + " is filtered out because duplication with " + duplicatedId);
                continue;
            } else {
                // console.log("New duplicated id is added " + duplicatedId + " from post " + docs[i]['id']);
                idSets.push(duplicatedId);
            }
        } else {
            if (idSets.indexOf(docs[i]['id']) != -1) {
                // console.log("Post " + docs[i]['id'] + " is filtered out");
                continue;
            }
        }

        postings[index] = {};

        postings[index]['id'] = docs[i]['id'];
        postings[index]['price'] = ParsePrices(docs[i]['price']);
        postings[index]['quantity'] = ParseQuantities(docs[i]['quantity']);
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

    console.log("Postings cache has " + postings.length + " entries")
}

pricesProvider.getPostings(function(error, docs) {
    UpdatePostingCache(error, docs);
    exports.postings = postings;
})

pricesProvider.getAllPrices(function(error, docs) {
    prices = docs;
    console.log("Prices cache has " + prices.length + " entries");
    exports.prices = prices;
})

// Periodically refresh cache every 5 minutes
setInterval(function() {
    pricesProvider.getPostings(function(error, docs) {
        UpdatePostingCache(error, docs);
        exports.postings = postings;
    })

    pricesProvider.getAllPrices(function(error, docs) {
        prices = docs;
        console.log("Prices cache has " + prices.length + " entries")
        exports.prices = prices;
    })
}, 300000);

var states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];
exports.states = states;

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.rawHTMLProvider = rawHTMLProvider;
exports.commonHelper = commonHelper;


