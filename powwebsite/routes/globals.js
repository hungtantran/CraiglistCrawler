var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();
var RawHTMLProvider = require('./rawhtmlprovider').RawHTMLProvider;
var rawHTMLProvider = new RawHTMLProvider();

function ReplaceAll (find, replace, str) {
  return str.replace(new RegExp(find, 'g'), replace);
}

function sortNumber(a,b) {
    return a - b;
}

function isIntValue(value){ 
    if ((parseFloat(value) == parseInt(value)) && !isNaN(value)){
      return true;
    } else { 
      return false;
    } 
}

function ParseArrayString(str, split, lowerBound, upperBound, isInt) {
    if (!str) {
        return str;
    }

    var array = [];
    if (str[0] == '[') {
        str = str.substring(1);
    }
    if (str[str.length - 1] == ']') {
        str = str.substring(0, str.length - 1);
    }
    array = str.split(split);

    var sortedArray = [];
    for (var i = 0; i < array.length; ++i) {
        var parsedValue = parseFloat(array[i]);
        if (!isNaN(parsedValue)) {
            if (!isInt || isIntValue(parsedValue)) {
                sortedArray.push(parsedValue);
            }
        }
    }
    sortedArray.sort(sortNumber);

    var uniqueSortedArray = [];
    for (var i = 0; i < sortedArray.length; ++i) {
        if (!isNaN(sortedArray[i]) && sortedArray[i] >= lowerBound && sortedArray[i] <= upperBound && (i == 0 || sortedArray[i] != sortedArray[i-1])) {
            var divisibleBy125 = Math.round(sortedArray[i] * 1000) % 25;
            if (divisibleBy125 == 0) {
                uniqueSortedArray.push(sortedArray[i]);
            }
        }
    }

    return uniqueSortedArray;
}

// For ex: [10.0, 4.0, 40.0, 7.0, 60.0, 14.0, 100.0, 1.0, 28.0, 200.0, 4.0, 112.0, 650.0, 253.0, 414.0, 4380.0]
function ParsePrices(prices) {
    if (!prices) {
        return prices;
    }

    var priceArray = ParseArrayString(prices, ',', 3, 1000, true);

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

    var gramArray = ParseArrayString(quantitiesType[1], ' ', 1.1, 200, false);
    quantitiesArray.push(gramArray);

    // One empty, one for gram, one for oz
    if (quantitiesType.length < 3) {
        return quantitiesArray;
    }
    var ounceArray = ParseArrayString(quantitiesType[2], ' ', 0.1, 200, false);
    quantitiesArray.push(ounceArray);

    return quantitiesArray;
}

var postings = [];

// function UpdateCache(error, docs) {
//     if (error != null) {
//         console.error('error to get postings: ' + error.stack);
//         process.exit(1);
//     }

//     postings = [];

//     for (var i = 0; i < docs.length; ++i)
//     {
//         postings[i] = {};

//         postings[i]['id'] = docs[i]['id'];
//         postings[i]['price'] = docs[i]['price'];
//         postings[i]['quantity'] = docs[i]['quantity'];
//         postings[i]['unit'] = docs[i]['unit'];
//         postings[i]['state'] = docs[i]['state'];
//         postings[i]['city'] = docs[i]['city'];
//         postings[i]['datePosted'] = docs[i]['datePosted'];
//         postings[i]['title'] = docs[i]['title'];
//         postings[i]['url'] = ReplaceAll(' ', '-', postings[i]['title']);
//         postings[i]['url'] += "-" + postings[i]['id'];

//         postings[i]['lat'] = docs[i]['lat1'];
//         postings[i]['lng'] = docs[i]['lng1'];

//         if (postings[i]['lat'] == null || postings[i]['lng'] == null) {
//             postings[i]['lat'] = docs[i]['lat2'];
//             postings[i]['lng'] = docs[i]['lng2'];
//         }
//     }

//     console.log("Cache has " + postings.length + " entries")
// }

// pricesProvider.getPostings(function(error, docs) {
//     UpdateCache(error, docs);
//     exports.postings = postings;
// })

function UpdateCache(error, docs) {
    if (error != null) {
        console.error('error to get postings: ' + error.stack);
        process.exit(1);
    }

    postings = [];

    for (var i = 0; i < docs.length; ++i)
    {
        postings[i] = {};

        postings[i]['id'] = docs[i]['id'];
        postings[i]['price'] = ParsePrices(docs[i]['price']);
        postings[i]['quantity'] = ParseQuantities(docs[i]['quantity']);
        postings[i]['state'] = docs[i]['state'];
        postings[i]['city'] = docs[i]['city'];
        postings[i]['datePosted'] = docs[i]['datePosted'];
        postings[i]['title'] = docs[i]['title'];
        postings[i]['url'] = ReplaceAll(' ', '-', postings[i]['title']);
        postings[i]['url'] += "-" + postings[i]['id'];

        postings[i]['lat'] = docs[i]['lat1'];
        postings[i]['lng'] = docs[i]['lng1'];

        if (postings[i]['lat'] == null || postings[i]['lng'] == null) {
            postings[i]['lat'] = docs[i]['lat2'];
            postings[i]['lng'] = docs[i]['lng2'];
        }
    }

    console.log("Cache has " + postings.length + " entries")
}

pricesProvider.getPostings(function(error, docs) {
    UpdateCache(error, docs);
    exports.postings = postings;
})

// Periodically refresh cache every 5 minutes
setInterval(function() {
    pricesProvider.getPostings(function(error, docs) {
        UpdateCache(error, docs);
        exports.postings = postings;
    })
}, 300000);

var states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];
exports.states = states;

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.rawHTMLProvider = rawHTMLProvider;


