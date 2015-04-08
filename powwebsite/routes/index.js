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
    title: 'Weed Price Index',
    stylesheet: '/stylesheets/index.css',
    markers: globals.locations,
    postings: globals.postings,
    states: globals.states,
    pricesString: globals.commonHelper.constructPriceStringArray(globals.postings),
    quantitiesString: globals.commonHelper.constructQuantityStringArray(globals.postings),
    description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in your area!',
    keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
    icon: '/public/images/icon.gif'
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

    stateInfo = [];
    for (var i = 0; i < globals.postings.length; ++i) {
        if (globals.postings[i]['state'].toUpperCase() === state.toUpperCase()) {
            stateInfo.push(globals.postings[i]);
        }
    }

    res.cookie('state', state);
    console.log();

    res.render('index', {
        title: 'Weed Price Index in ' + state,
        stylesheet: '/stylesheets/index.css',
        markers: stateInfo,
        postings: stateInfo,
        states: globals.states,
        stateChosen: state,
        pricesString: globals.commonHelper.constructPriceStringArray(stateInfo),
        quantitiesString: globals.commonHelper.constructQuantityStringArray(stateInfo),
        description: 'Looking to buy weed? LeafyExchange can help you find the best prices of weed, marijuana pot in ' + state,
        keywords: '420,weed,pot,marijuana,green,price of weed, price of pot, price of marijuana, legalize, medical, medicinal, herb, herbal',
        icon: '/public/images/icon.gif'
  });
});

module.exports = router;
