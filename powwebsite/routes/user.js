var express         = require('express');
var globals         = require('./globals');
var router          = express.Router();

var globals         = require('./globals');
var userProvider   = globals.userProvider;

// Santity check purchase order request
function sanityCheckRequest(request) {
  if (request === null || request === undefined) {
    console.log("undefined");
    return false;
  }

  if (!('email' in request) ||
    !('username' in request) ||
    !('password' in request) ||
    !('retypedPassword' in request)) {
    console.log("missing fields");
    console.log(request);
    return false;
  }

  if (request['password'] != request['retypedPassword']) {
    console.log('Passwords do not match');
    return false;
  }

  return true;
}

// Sign Up
router.post('/signup', function(req, res) {
    var responseJson = {};

    // Sanity check
    if (!sanityCheckRequest(req.body))
    {
      console.log('Request format invalid');
      responseJson['result'] = false;
      responseJson['message'] = 'Request format invalid';
    } else {
      userProvider.insertUser(
        req.body['email'],
        req.body['username'],
        req.body['password'],
        function(error, doc) {
          if (error) {
            console.log(error);
          }
        }
      );

      res.statusCode = 302;
      res.setHeader('Location', '/');
      res.end();
    }
});

// Log In
router.post('/login', function(req, res) {
  if (req.session.logged) {
    // TODO sth 
  } else {
    userProvider.getUser(
      req.body['username'],
      req.body['password'],
      function(error, doc) {
        if (error) {
          console.log(error);
        } else {
          if (doc && doc.length == 1) {
            req.session.logged = true;
            req.session.user = doc[0];
            console.log('log in successfully');
          } else {
            console.log(doc);
            console.log('log in fail');
          }
        }

        res.statusCode = 302;
        res.setHeader('Location', '/');
        res.end();
      }
    );
  }
});

// Log Out
router.post('/logout', function(req, res) {
  if (req.session.logged) {
    req.session.logged = false;
  }

  res.statusCode = 302;
  res.setHeader('Location', '/');
  res.end();
});

router.get('/logout', function(req, res) {

  if (req.session.logged) {
    req.session.logged = false;
  }

  res.statusCode = 302;
  res.setHeader('Location', '/');
  res.end();
});

module.exports = router;