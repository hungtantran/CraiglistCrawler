var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');
var postingLocationProvider   = globals.postingLocationProvider;
var commonHelper = globals.commonHelper;

// Get all posts in history
router.get('/', function(req, res) {
    postingLocationProvider.getResults(0, 15, function(error, docs) {
        for (var i = 0; i < docs.length; ++i)
        {
            docs[i]['url'] = commonHelper.ReplaceAll(' ', '-', docs[i]['title']);
            docs[i]['url'] += "-" + docs[i]['id'];
            docs[i]['price'] = commonHelper.ParsePrices(docs[i]['price']);
            docs[i]['quantity'] = commonHelper.ParseQuantities(docs[i]['quantity']);
        }

        var pricesString = globals.commonHelper.constructPriceStringArray(docs);
        var quantitiesString = globals.commonHelper.constructQuantityStringArray(docs);
        console.log(quantitiesString);

        res.render('search', {
            title: "Search results for all marijuana sale posts",
            stylesheet: '/stylesheets/posting.css',
            results: docs,
            pricesString: pricesString,
            quantitiesString: quantitiesString,
            states: globals.states,
            description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area',
            keywords: 'price of weed, price of marijuana, price of pot, 420, green, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
            icon: '/images/leafyexchange.jpg'
        });
    })
});

// Get page with search pararm
router.get('/:params', function(req, res) {
});

module.exports = router;