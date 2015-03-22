var express         = require('express');
var router          = express.Router()
;
var globals         = require('./globals');
var rawHTMLProvider   = globals.rawHTMLProvider;

router.post('/postingbody/:id', function(req, res) {
    var params = req.params.id.split("-");
    var id = params[params.length-1];

    rawHTMLProvider.getContent(id, function(error, doc) {
        res.json(doc);
    })
});

// Get the main page with a list of topics
router.get('/:id', function(req, res) {
    var params = req.params.id.split("-");
    var id = params[params.length-1];

    rawHTMLProvider.getContent(id, function(error, doc) {
        
        res.render('posting', {
            title: 'Weed Posting Page',
            stylesheet: '/stylesheets/posting.css',
            content: doc['posting_body']
        });
        return;
    })
});

module.exports = router;