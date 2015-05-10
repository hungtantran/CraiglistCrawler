var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');
var postingLocationProvider   = globals.postingLocationProvider;

router.post('/postingbody/:id', function(req, res) {
    var params = req.params.id.split('-');
    var id = params[params.length-1];

    if (isNan(id)) {
        res.json("");
        return;
    }

    postingLocationProvider.getContent(id, function(error, doc) {
        res.json(doc);
    })
});

// Get the main page with a list of topics
router.get('/:id', function(req, res) {
    var params = req.params.id.split('-');
    var id = params[params.length-1];

    if (isNaN(id)) {
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    postingLocationProvider.getContent(id, function(error, doc) {
        if (error || doc === undefined || !('posting_body' in doc)) {
            res.statusCode = 302;
            res.setHeader('Location', '/');
            res.end();
            return;
        }

        // Related posts from the same city
        var info = [];
        var city = doc['city'];
        for (var i = 0; i < globals.postings.length; ++i) {
            if (globals.postings[i]['city'].toUpperCase() === city.toUpperCase()) {
                info.push(globals.postings[i]);
            }
        }

        // Title of the page
        var title = doc['title'];
        if (title === undefined || title === null || title.length === 0) {
            title = 'Weed Posting Page';
        }
        title += " - LeafyExchange";

        // Calculate type and strains of post
        var types = {};
        for (var i = 0; i < globals.postingTags.length; ++i) {
            if (globals.postingTags[i]['id'] == id) {
                types[globals.postingTags[i]['type_id']] = true;
            }
        }

        for (var i = 0; i < globals.allTypes.length; ++i) {
            if (globals.allTypes[i]['id'] in types) {
                types[globals.allTypes[i]['id']] = globals.allTypes[i]['name'];
            }
        }

        res.render('posting', {
            title: title,
            stylesheet: '/stylesheets/posting.css',
            content: doc['posting_body'],
            url: doc['url'],
            state: doc['state'],
            city: city,
            relatedPosts: info,
            pricesString: globals.commonHelper.constructPriceStringArray(info),
            quantitiesString: globals.commonHelper.constructQuantityStringArray(info),
            latitude: doc['latitude'],
            longitude: doc['longitude'],
            states: globals.states,
            types: types,
            description: 'Looking for weed? ' + doc['title'] + '. LeafyExchange has the best prices of weed, marijuana pot in ' + doc['city'] + ', ' + doc['state'],
            keywords: 'price of weed, price of marijuana, price of pot, 420, green, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
            icon: '/images/leafyexchange.jpg',
            javascriptSrcs:
                ['http://maps.googleapis.com/maps/api/js',
                 'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js',
                 'http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js',
                 'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
                 '/javascripts/posting.js']
        });
        return;
    })
});

module.exports = router;