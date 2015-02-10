var express         = require('express');
var router          = express.Router();

var globals         = require('./globals');

// Get all location json
router.get('/', function(req,res){
    res.json(globals.locations);
});

module.exports = router;