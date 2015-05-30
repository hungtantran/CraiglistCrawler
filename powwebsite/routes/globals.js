var filesystem      = require('fs')

var PostingLocationProvider = require('./postinglocationprovider').PostingLocationProvider;
var postingLocationProvider = new PostingLocationProvider();
var PricesProvider = require('./pricesprovider').PricesProvider;
var pricesProvider = new PricesProvider();
var LocalBusinessProvider = require('./localBusinessProvider').LocalBusinessProvider;
var localBusinessProvider = new LocalBusinessProvider();
var TypeProvider = require('./typeProvider').TypeProvider;
var typeProvider = new TypeProvider();
var StrainProvider = require('./strainProvider').StrainProvider;
var strainProvider = new StrainProvider();
var NewsProvider = require('./newsProvider').NewsProvider;
var newsProvider = new NewsProvider();
var UserProvider = require('./userProvider').UserProvider;
var userProvider = new UserProvider();

var CommonHelper = require('./commonHelper').CommonHelper;
var commonHelper = new CommonHelper();
var PeriodicProcess = require('./periodicProcess').PeriodicProcess;
var periodicProcess = new PeriodicProcess();

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
    });

    localBusinessProvider.getAllLocalBusinesses(function(error, docs) {
        localBusinesses = docs;
        console.log("Local business cache has " + localBusinesses.length + " entries")
        exports.localBusinesses = localBusinesses;
    });

    pricesProvider.getAllPrices(function(error, docs) {
        prices = docs;
        console.log("Prices cache has " + prices.length + " entries");
        exports.prices = prices;
    });

    pricesProvider.getAllPostingLocations(function(error, docs) {
        postingLocations = docs;
        console.log("PostingLocations cache has " + postingLocations.length + " entries");
        exports.postingLocations = postingLocations;
    });

    strainProvider.getAllActivePostStrain(function(error, docs) {
        postingTags = docs;
        console.log("postingTags cache has " + postingTags.length + " entries");
        exports.postingTags = postingTags;
    });

    newsProvider.getAllNews(function(error, docs) {
        news = docs;
        for (var i = 0; i < news.length; i++) {
            news[i]['url'] = 'news/' + news[i]['title'].replace(/ /g, '-') + '-' + news[i]['id'];
        }
        console.log("news cache has " + news.length + " entries");
        exports.news = news;
    });
}

RefreshCache();

// Periodically refresh cache every 1 hours
setInterval(function() {
    RefreshCache();
}, 3600000);

function GetEmailTemplate() {
    filesystem.readFile(__dirname + "/sellerEmail.html", "utf-8", function(error, data) {
        if (error) {
          // TODO: log error
        } else {
            exports.emailTemplate =data ;
        }
    });
}
GetEmailTemplate();

function GetAllTypesAndStrains() {
    typeProvider.getAllTypes(function(error, docs) {
        allTypes = docs;
        console.log("AllTypes has " + allTypes.length + " entries")
        exports.allTypes = allTypes;
    });

    strainProvider.getAllStrains(function(error, docs) {
        allStrains = docs;
        console.log("AllStrains has " + allStrains.length + " entries")
        exports.allStrains = allStrains;
    });
}
GetAllTypesAndStrains();

var states = ['Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia', 'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts', 'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey', 'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island', 'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia', 'Wisconsin', 'Wyoming'];
exports.states = states;

exports.pricesProvider = pricesProvider;
exports.postingLocationProvider = postingLocationProvider;
exports.userProvider = userProvider;
exports.commonHelper = commonHelper;
exports.periodicProcess = periodicProcess;