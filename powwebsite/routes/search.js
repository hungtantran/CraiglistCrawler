var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');
var postingLocationProvider   = globals.postingLocationProvider;
var commonHelper = globals.commonHelper;

// Get all posts in history
router.get('/', function(req, res) {
    postingLocationProvider.getResults(0, 15, function(error, doc) {
        for (var i = 0; i < doc.length; ++i)
        {
            doc[i]['url'] = commonHelper.ReplaceAll(' ', '-', doc[i]['title']);
            doc[i]['url'] += "-" + doc[i]['id'];
        }

        res.render('search', {
            title: "Search results for all marijuana sale posts",
            stylesheet: '/stylesheets/posting.css',
            results: doc,
            pricesString: globals.commonHelper.constructPriceStringArray(doc),
            quantitiesString: globals.commonHelper.constructQuantityStringArray(doc),
            states: globals.states,
            description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area',
            keywords: 'price of weed, price of marijuana, price of pot, 420, green, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
            icon: '/images/leafyexchange.jpg'
        });
    })
});

// Get page with search pararm
router.get('/:id', function(req, res) {
});

module.exports = router;