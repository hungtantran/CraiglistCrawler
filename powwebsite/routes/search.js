var express         = require('express');
var router          = express.Router();
;
var url = require('url');

var globals         = require('./globals');
var postingLocationProvider   = globals.postingLocationProvider;
var commonHelper = globals.commonHelper;

var MAX_RESULT_PER_PAGE = 25;

// Get all posts in history
router.get('/', function(req, res) {
    var page = 1;
    var term = null;

    var queryData = url.parse(req.url, true).query;
    if ('page' in queryData) {
        if (!isNaN(queryData['page'])) {
            page = parseInt(queryData['page']);
        }

        if (page < 1) {
            page = 1;
        }
    }

    var previousPageLink = '/search/?page='+(page-1);
    var nextPageLink = '/search/?page='+(page+1);
    if ('term' in queryData) {
        term = queryData['term'];
        term = term.trim();

        if (!term) {
            term = null;
        } else {
            previousPageLink += '&term='+term;
            nextPageLink += '&term='+term;
        }
    }

    var lowerBound = (page-1) * MAX_RESULT_PER_PAGE;

    postingLocationProvider.getResults(lowerBound, MAX_RESULT_PER_PAGE, function(error, docs) {
        for (var i = 0; i < docs.length; ++i)
        {
            docs[i]['url'] = commonHelper.ReplaceAll(' ', '-', docs[i]['title']);
            docs[i]['url'] += "-" + docs[i]['id'];
            docs[i]['price'] = commonHelper.ParsePrices(docs[i]['price']);
            docs[i]['quantity'] = commonHelper.ParseQuantities(docs[i]['quantity']);
        }

        var pricesString = globals.commonHelper.constructPriceStringArray(docs);
        var quantitiesString = globals.commonHelper.constructQuantityStringArray(docs);

        res.render('search', {
            title: "Search results for all marijuana sale posts",
            stylesheet: '/stylesheets/search.css',
            results: docs,
            pricesString: pricesString,
            quantitiesString: quantitiesString,
            page: page,
            term: term,
            previousPageLink: previousPageLink,
            nextPageLink: nextPageLink,
            states: globals.states,
            description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area',
            keywords: 'price of weed, price of marijuana, price of pot, 420, green, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
            icon: '/images/icon.png'
        });
    })
});

module.exports = router;