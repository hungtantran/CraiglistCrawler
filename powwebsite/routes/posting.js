var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');
var rawHTMLProvider   = globals.rawHTMLProvider;

// Get the main page with a list of topics
router.get('/:id', function(req, res) {
    var params = req.params.id.split("-");
    var id = params[params.length-1];
    console.log('posting-id = '+id);

    rawHTMLProvider.getContent(id, function(error, doc) {
        console.log('html = '+doc);
        res.render('posting', {
            title: 'Weed Posting Page',
            content: doc
        });
        return;
    })
});

module.exports = router;