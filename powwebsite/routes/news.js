var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');

router.get('/:id', function(req, res) {
    var params = req.params.id.split('-');
    var id = params[params.length-1];

    if (isNaN(id)) {
        res.statusCode = 404;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    var index = null;
    for (var i = 0; i < globals.news.length; i++) {
        if (globals.news[i]['id'] == id) {
            console.log('here')
            index = i;
            break;
        }
    }

    if (index === null) {
        res.statusCode = 404;
        res.setHeader('Location', '/');
        res.end();
        return;
    }

    res.render('news_page', {
        title: globals.news[index]['title'] + ' - LeafyExchange',
        stylesheet: '/stylesheets/news_page.css',
        session: req.session,
        states: globals.states,
        news: globals.news[index],
        description: globals.news[index]['summary'],
        keywords: globals.news[index]['keywords'],
        icon: '/images/icon.png',
        javascriptSrcs: ['http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js']
    });
});

// Get the main page with a list of news
router.get('/', function(req, res) {
    res.render('news', {
        title: 'LeafyExchange: Marijuana News & Information',
        stylesheet: '/stylesheets/news.css',
        session: req.session,
        states: globals.states,
        allNews: globals.news,
        description: 'Marijuana news, information, and culture by LeafyExchange, The Best Weed Prices and Delivery Source',
        keywords: 'marijuana news, information, culture, weed delivery, marijuana delivery, cannabis, weed, pot, marijuana, legalize, medical, medicinal, herb, herbal',
        icon: '/images/icon.png',
        javascriptSrcs: ['http://cdn.jsdelivr.net/d3js/3.3.9/d3.min.js']
    });
});

module.exports = router;