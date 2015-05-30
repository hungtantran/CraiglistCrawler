var express = require('express');
var router = express.Router();

var globals = require('./globals');

/* GET home page. */
router.get('/', function(req, res) {
  // Redirect to state page if state cookie exists
  if (req.cookies !== undefined
    && req.cookies['state'] !== undefined
    && req.cookies['state'].length != 0)
  {
    var url = '/state/' + req.cookies['state'];
    res.statusCode = 302;
    res.setHeader('Location', url);
    res.end();
    return;
  }

  res.render('index', {
    title: 'The Best Weed Prices and Delivery Source - LeafyExchange',
    stylesheet: '/stylesheets/index.css',
    session: req.session,
    postings: globals.postings,
    localBusinesses: globals.localBusinesses,
    states: globals.states,
    pricesString: globals.commonHelper.constructPriceStringArray(globals.postings),
    quantitiesString: globals.commonHelper.constructQuantityStringArray(globals.postings),
    description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area!',
    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
    icon: '/images/icon.png',
    javascriptSrcs: 
        ['http://maps.googleapis.com/maps/api/js?libraries=places&sensor=false',
         'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js',
         'http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js',
         'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
         '/javascripts/index.js']
  });
});

// Get some state
router.get('/state/:state', function(req, res){
    var state = req.params.state;

    // If users choose all state, clear cookie and redirect to homepage.
    if (state =='all') {
        res.cookie('state', '');
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    // Check if the state param passed in exists, if not redirect back
    // to homepage, which might redirect to a state page.
    var stateExists = false;
    for (var i = 0; i < globals.states.length; ++i) {
        if (globals.states[i].toUpperCase() === state.toUpperCase()) {
            stateExists = true;
        }
    }

    if (!stateExists) {
        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    var postingStateInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
            postingStateInfo.push(globals.postings[i]);
        }
    }

    localBusinessStateInfo = [];
    for (var i = 0; i < globals.localBusinesses.length; ++i) {
        if (globals.localBusinesses[i]['state'].toUpperCase() === state.toUpperCase()) {
            localBusinessStateInfo.push(globals.localBusinesses[i]);
        }
    }

    res.cookie('state', state);

    res.render('index', {
        title: 'The Best Weed Delivery Source in ' + state + ' - LeafyExchange',
        stylesheet: '/stylesheets/index.css',
        session: req.session,
        postings: postingStateInfo,
        localBusinesses: localBusinessStateInfo,
        states: globals.states,
        stateChosen: state,
        pricesString: globals.commonHelper.constructPriceStringArray(postingStateInfo),
        quantitiesString: globals.commonHelper.constructQuantityStringArray(postingStateInfo),
        description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in ' + state,
        keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
        icon: '/images/icon.png',
        javascriptSrcs: 
            ['http://maps.googleapis.com/maps/api/js',
             'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js',
             'http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js',
             'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerwithlabel/src/markerwithlabel_packed.js',
             '/javascripts/index.js']
  });
});

/* GET privacy page. */
router.get('/privacy', function(req, res) {
  res.render('privacy', {
    title: 'Privacy - LeafyExchange',
    stylesheet: '/stylesheets/index.css',
    session: req.session,
    states: globals.states,
    description: 'Looking for the price of weed? LeafyExchange can help you find the prices of pot in your area!',
    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
    icon: '/images/icon.png'
  });
});

/* GET about us page. */
router.get('/aboutus', function(req, res) {
  res.render('aboutus', {
    title: 'About Us - LeafyExchange',
    stylesheet: '/stylesheets/index.css',
    session: req.session,
    states: globals.states,
    description: 'Looking for the price of weed? LeafyExchange can help you find the prices of pot in your area!',
    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
    icon: '/images/icon.png'
  });
});

/* GET terms and conditions page. */
router.get('/terms', function(req, res) {
  res.render('terms', {
    title: 'Terms and Conditions - LeafyExchange',
    stylesheet: '/stylesheets/index.css',
    session: req.session,
    states: globals.states,
    description: 'Looking for the price of weed? LeafyExchange can help you find the prices of pot in your area!',
    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
    icon: '/images/icon.png'
  });
});

module.exports = router;
