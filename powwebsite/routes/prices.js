var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.post('/', function(req,res){
    res.json(globals.prices);
});

module.exports = router;
