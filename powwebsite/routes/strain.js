var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');

router.post('/:strainName', function(req, res) {
    var strainName = req.params.strainName;
    strainName = strainName.replace(/-/g, ' ');
    console.log(strainName);

    var strain = null;
    for (var i = 0; i < globals.allStrains.length; ++i) {
        if (globals.allStrains[i]['name'].toUpperCase() === strainName.toUpperCase()) {
            strain = globals.allStrains[i];
            break;
        }
    }

    // Invalid strain
    if (strain === null) {
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    var postingTagsInfoId = [];
    for (var i = 0; i < globals.postingTags.length; ++i) {
        if (globals.postingTags[i]['strain_id'] === strain['id']) {
            postingTagsInfoId.push(globals.postingTags[i]['id']);
        }
    }

    var postingTagsInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (postingTagsInfoId.indexOf(globals.postings[i]['id']) !== -1) {
            postingTagsInfo.push(globals.postings[i]);
        }
    }

    res.json(postingTagsInfo);
});

// Get the main page with a list of topics
router.get('/:strainName', function(req, res) {
    var strainName = req.params.strainName;
    strainName = strainName.replace(/-/g, ' ');
    console.log(strainName);

    var strain = null;
    for (var i = 0; i < globals.allStrains.length; ++i) {
        if (globals.allStrains[i]['name'].toUpperCase() === strainName.toUpperCase()) {
            strain = globals.allStrains[i];
            break;
        }
    }

    // Invalid strain
    if (strain === null) {
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    var postingTagsInfoId = [];
    for (var i = 0; i < globals.postingTags.length; ++i) {
        if (globals.postingTags[i]['strain_id'] === strain['id']) {
            postingTagsInfoId.push(globals.postingTags[i]['id']);
        }
    }

    var postingTagsInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (postingTagsInfoId.indexOf(globals.postings[i]['id']) !== -1) {
            postingTagsInfo.push(globals.postings[i]);
        }
    }

    var description = '';
    if (strain['description'] !== null) {
        description = strain['description'];
    }

    res.render('strain', {
        title: 'LeafyExchange: The Best ' + strain['name'] + ' Prices and Delivery Source',
        stylesheet: '/stylesheets/strain.css',
        session: req.session,
        strain: strain,
        postings: postingTagsInfo,
        pricesString: globals.commonHelper.constructPriceStringArray(postingTagsInfo),
        quantitiesString: globals.commonHelper.constructQuantityStringArray(postingTagsInfo),
        states: globals.states,
        description: 'Looking for ' + strain['name'] + ' Weed? LeafyExchange has the best prices of ' + strain['name'] + ' Weed in the US. ' + description,
        keywords: strain['name'] + ' Weed, price of ' + strain['name'] + ' Weed, buy ' + strain['name'] + ' Weed, sell ' + strain['name'] +' Weed, 420, green, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
        icon: '/images/icon.png',
        javascriptSrcs:
            ['http://maps.googleapis.com/maps/api/js',
             'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
             '/javascripts/strain.js']
    });
});

module.exports = router;