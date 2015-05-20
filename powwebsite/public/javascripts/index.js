function codeLatLng(lat, lng) {
  var latlng = new google.maps.LatLng(lat, lng);
  geocoder.geocode({'latLng': latlng}, function(results, status) {
    if (status === google.maps.GeocoderStatus.OK) {
      console.log(results);
    } else {
      console.log("Geocoder failed due to: " + status);
    }
  });
}

function updateNewDisplay(displayInfo, intializeParams) {

}

// Function that reupdate the display with the newest filter
function updateDisplay(displayInfo, initializeParams) {
  if (displayInfo === undefined) {
    displayInfo = cache;
  }

  var isInitializePosting = true;
  var isInitializeMap = true;
  var isInitializePrice = true;
  var isInitializeLocalBusiness = true;
  if (initializeParams !== undefined) {
    isInitializePosting = initializeParams[0];
    isInitializeMap = initializeParams[1];
    isInitializePrice = initializeParams[2];
    isInitializeLocalBusiness = initializeParams[3];
  }

  if (isInitializePosting === undefined || isInitializePosting) {
    initializePostings(displayInfo['postings']);
  }

  if (isInitializeLocalBusiness === undefined || isInitializeLocalBusiness) {
    initializeLocalBusinesses(displayInfo['localBusinesses']);
  }

  if (isInitializeMap === undefined || isInitializeMap) {
    initializeMap(displayInfo['postings']);
  }

  if (isInitializePrice === undefined || isInitializePrice) {
    initializePrices(displayInfo['prices']);
  }
}

function initializePrices(prices) {
  $('#price_bin_dist_by_state').empty();
  $('#svgAxis').remove();

  var params = document.URL.split("/");
  var id = params[params.length-1];
  var postingPrices = [];
  var isPostingPage = false;
  if (!isNaN(parseInt(id))) isPostingPage = true;

  newPrices = [];
  for (var i = 0; i < prices.length; ++i) {
    if (isPostingPage && prices[i]['price_fk'] === id) {
      postingPrices.push(prices[i]);
    }

    lat = prices[i]['lat'];
    lng = prices[i]['lng'];

    var priceLocation = new google.maps.LatLng(lat, lng)
    if (mapBound === null || mapBound === undefined || mapBound.contains(priceLocation)) {
      newPrices.push(prices[i]);
    }
  }

  newPriceBin('price_bin_dist_by_state', newPrices);
}

function newPriceBin(divId, prices) {
  var binNames = ['0-5', '5-10', '10-15', '15-20', '20-25', '25-30', '30-35', '35-40','40-45','45+'];
  var data = {}
  for (var i=0; i<prices.length; ++i)
  {
    var entry = prices[i];
    var state = entry['state']
    var price = entry['price']
    var unit = entry['unit'];
    var quantityGrams = entry['quantity'];
    // convet ounces to grams
    if (unit=='oz') { quantityGrams = quantityGrams * 28.3495; }
    var pricePerGram = price / quantityGrams;

    // find appropriate state bin to increment
    if (!(state in data)) {
      data[state] = {};
      binNames.map( function(name) { data[state][name] = 0 });
    }

    // increment that bin
    bins = data[state];
    var bin = Math.floor(pricePerGram / 5);
    if (bin > 9) { bin = 9; }
    
    var binKey = binNames[bin]
    bins[binKey] = bins[binKey] + 1;
  }

  var numTotalBins = 0;
  var numEmptyBins = 0;
  arr = [];
  for (k in data) {
    data[k]['state'] = k;
    data[k]['bins'] = binNames.map( function(name) { return {name:name, value:data[k][name], state:k} });

    arr.push(data[k]);

    for (var i=0; i<data[k]['bins'].length; ++i) {
      if (data[k]['bins'][i]['value'] == 0) {
        numEmptyBins++;
      }
      numTotalBins++;
    }
  }

  var margin = {top: 20, right: 40, bottom: 30, left: 40},
    width = $('#' + divId).width()- margin.left*2.5 - margin.right*2.5,
    height = 250*arr.length * ((numTotalBins-numEmptyBins) / numTotalBins);
  
  var y0 = d3.scale.ordinal()
    .rangeRoundBands([0, height], .1);

  var y1 = d3.scale.ordinal();

  var x = d3.scale.linear()
      .range([0, width]);

  var color = d3.scale.ordinal()
      .range(["#A2c295","#88b57a","#6DA964","#4C9D51","#3B9748","#319412","#29803A","#217B37","#25803A","#1D7634","#187131","#136C2F","#0E672C","#09622A"]);

  var xAxis = d3.svg.axis()
      .scale(x)
      .orient("top")
      .tickSize(0);

  var yAxis = d3.svg.axis()
      .scale(y0)
      .orient("left");

  var yAxisBins = d3.svg.axis()
      .scale(y1)
      .orient("left")
      .tickSize(0);

  var tooltip = d3.select('#price_bin')
    .append('div')
    .attr('class', 'tooltip');

  tooltip.append('div')
    .attr('class', 'state');
  tooltip.append('div')
    .attr('class', 'label');
  tooltip.append('div')
    .attr('class', 'count');

  var svgAxis = d3.select('#' + 'price_bin').insert("svg", ":first-child")
      .attr("width", "100%")
      .attr("height", 18)
      .attr("id", "svgAxis")
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  var svg = d3.select('#' + divId).append("svg")
      .attr("width", "100%")
      .attr("height", height + margin.top + margin.bottom)
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  y0.domain(Object.keys(data));
  y1.domain(binNames).rangeRoundBands([0, y0.rangeBand()]);
  x.domain([0, d3.max(arr, function(d) { return d3.max(d['bins'], function(d) { return d.value; }); })]);

  svgAxis.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(100,0)")
    .style("position", "fixed")
    .call(xAxis);

  svg.append("g")
      .attr("class", "y axis")
      .attr("transform", "translate("+50+",0)")
      .call(yAxis);

  var state = svg.selectAll(".state")
    .data(arr)
      .enter().append("g")
        .attr("class", "g")
        .attr("transform", function(d) { return "translate(100," + y0(d.state) + ")"; });

  var axisFunc = function(d, binNames, y0, y1, data) {
    var bins = [];
    var __data__ = d[0][0]["__data__"];
    var state = __data__["state"];
    for (var i=0; i<binNames.length; ++i) {
      var binName = binNames[i];
      if (__data__[binName] > 0) {
        bins.push(binName);
      }
    }

    var yAxisBins = d3.svg.axis()
      .scale(y1)
      .orient("left")
      .tickSize(0);
    y1.domain(bins).rangeRoundBands([0, y0.rangeBand()]);

    return yAxisBins(d);
  }

  state.append("g")
    .attr("class", "y axis")
    .attr("transform", "translate(0,0)")
    .call(axisFunc, binNames, y0, y1, data);

  state.selectAll("rect")
    .data(function(d) {
      return d['bins']; })
  .enter().append("text")
    .text(function(d) { 
      if (d.value === null) {
        return 0;
      } else {
        if (d.value === 0) return "";
        return d.value;
      }
    })
    .attr("x", function(d) {
      if (d.value === null) {
        return 0;
      } else {
        return x(d.value) + 4;
      }
    })
    .attr("y", function(d) {
      return y1(d.name) + 4 + y1.rangeBand()/2;
    });
    

  state.selectAll("rect")
      .data(function(d) {
        return d['bins']; })
    .enter().append("rect")
      .attr("width", function(d) { 
        if (d.value === null) {
          return 0;
        } else {
          return x(d.value);
        }
      })
      .attr("x", 1)
      .attr("y", function(d) {
        console.log(y1.domain());
        return y1(d.name)+2;
      })
      .attr("height", function(d) {
        console.log(y0.rangeBand());
        console.log(y1.rangeBand());
        // return 10;
        return y1.rangeBand()-4;
      })//y1.rangeBand()-4)
      .on('mouseover', function(d) {
        var xPosition = parseInt(d3.select(this).attr("x") );
        var yPosition = parseInt(d3.select(this).attr("y") );

        tooltip.select(".state").html(d.state);
        tooltip.select(".label").html("Price Range: " + d.name);
        tooltip.select('.count').html("Number of postings:" + d.value);
        tooltip.style('left', xPosition + "px");
        tooltip.style('top', yPosition + "px");
        tooltip.style('display', 'block');
        })
      .on('mouseout', function(d) {
        tooltip.style('display', 'none');
        })
      .on('click', function(d) {
        if (stateFilter === d.state) stateFilter = null;
        else stateFilter = d.state;
        updateDisplay();
        })
      .style("fill", function(d) {
        if (stateFilter === null || d.state === stateFilter) {
          return color(d.name);
        } else {
          return "#F5F5F5";
        }
        });
}

// Callback function to handle change in map
function handleMapChange() {
  if(typeof timeoutID === "number") {
    window.clearTimeout(timeoutID);
    delete timeoutID;
  }

  timeoutID = window.setTimeout(function() {
    var newMapBound = map.getBounds();
    // Only update when map bound change
    if (mapBound === null || !newMapBound.equals(mapBound)) {
      mapBound = newMapBound;
      updateDisplay();
    }
  }, 1000);
}

// Function that draw marker on map
function drawMarker(map, markers) {
  if (markerClusterer != null) {
    markerClusterer.clearMarkers();
  }

  // Clear all existing markers first
  for (var i = 0; i < markerArray.length; i++) {
    markerArray[i].setMap(null);
  }

  markerArray.length = 0;

  for (var i = 0; i < markers.length; ++i)
  {
    if (stateFilter != null && markers[i]['state'] != stateFilter) {
      continue;
    }

    lat = markers[i]['lat'];
    lng = markers[i]['lng'];

    if (lat != null && lng != null) {
      var marker = new MarkerWithLabel({
        position: new google.maps.LatLng(lat, lng),
        map: map,
        title: markers[i]['title'],
        labelContent: i,
        labelClass: "labels" // the CSS class for the label
      });

      google.maps.event.addListener(marker, 'click', handleMapChange);

      markerArray.push(marker);
    }
  }

  markerClusterer = new MarkerClusterer(map, markerArray);
}

// Reinitialize the map, call to redraw the map
function initializeMap(markers, redrawMap) {
  // Initialize maps
  if (map === null || (redrawMap != null && redrawMap === true)) {
    map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
    google.maps.event.addListener(map, 'zoom_changed', handleMapChange);
  }

  // Initialize markers
  if (map !== null) {
    if (markers !== null) {
      drawMarker(map, markers);
    }

    if (state === null && curLatLng != null) {
      map.setCenter(curLatLng);
      map.setZoom(10);
      curLatLng = null;
    }

    mapBound = map.getBounds();
  }
}

// Function to create a new map
function newMap(latitude, longtitude, zoom, divId, draggable) {
  var draggable_ = typeof draggable ==='undefined' ? true : draggable;

  var mapOptions = {
    center: { lat: latitude, lng: longtitude},
    zoom: zoom,
    draggable: draggable_
  };

  map = new google.maps.Map(document.getElementById(divId),
    mapOptions);

  return map;
}

function initializePostings(postings) {
  if (postings === null || postings === undefined) {
    return;
  }

  var table = document.getElementById('table_body_posting');
  for(var i = table.rows.length - 1; i >= 0; --i)
  {
      table.deleteRow(i);
  }

  var totalSalePosts = 0;

  // Chrome, safara and ie has different order or json array
  var startIndex = 0;
  var endIndex = postings.length;
  var step = 1;
  if (postings.length > 1) {
    if (postings[postings.length-1]['datePosted'] < postings[0]['datePosted']) {
      startIndex = postings.length - 1;
      endIndex = -1;
      step = -1;
    }
  }

  for (var i = startIndex; i !== endIndex; i += step)
  {
    if (stateFilter != null && postings[i]['state'] != stateFilter) {
      continue;
    }

    if (!postings[i]['city']) {
      continue;
    }

    lat = postings[i]['lat'];
    lng = postings[i]['lng'];

    var postingLocation = new google.maps.LatLng(lat, lng);

    if (mapBound !== undefined && !mapBound.contains(postingLocation)) {
      continue;
    }

    var row = table.insertRow(table.length);
    
    var index = 0;

    // Date/Location Posted cell
    var dateLocationPosted = row.insertCell(index++);
    var url = '/posting/' + postings[i]['url'];
    dateLocationPosted.innerHTML = postings[i]['datePosted'] + " - " + postings[i]['city'];
    dateLocationPosted.setAttribute('class','col-xs-2 col-sm-2 col-md-2 col-lg-2');

    // Title cell
    var title = row.insertCell(index++);
    title.innerHTML = '<strong><a href="' + url + '">' + postings[i]['title'] + '</a></strong>';
    title.setAttribute('class','col-xs-8 col-sm-8 col-md-6 col-lg-6');

    // Code for query with grouping
    // Quantity cell
    var quantity = row.insertCell(index++);
    var quantityString = "";
    if (!postings[i]['quantity']) {
      // quantityString = 'Check Quantity!'
      quantityString = '.........';
    } else {
      var gramArray = postings[i]['quantity'][0];
      if (gramArray) {
        for (var j = 0; j < gramArray.length; ++j) {
          quantityString += gramArray[j] + "g, "
        }
      }

      var ounceArray = postings[i]['quantity'][1];
      if (ounceArray) {
        for (var j = 0; j < ounceArray.length; ++j) {
          quantityString += ounceArray[j] + "oz, "
        }
      }
    }

    quantity.innerHTML = quantityString;
    quantity.setAttribute('class','col-xs-2 col-sm-2 col-md-2 col-lg-2');

    // Price cell
    var price = row.insertCell(index++);

    var priceString = "";
    if (!postings[i]['price']) {
      // priceString = 'Check Price!';
      priceString = '.........';
    } else {
      for (var j = 0; j < postings[i]['price'].length; ++j) {
        if (j > 6) {
          priceString += "..."
          break;
        }
        priceString += "$" + postings[i]['price'][j] + ", "
      }
    }

    price.innerHTML = priceString;
    price.setAttribute('class','hidden-xs hidden-sm col-md-2 col-lg-2');

    ++totalSalePosts;
  }

  if (totalSalePosts === 0) {
    var row = table.insertRow(table.length);
    var noPostLink = row.insertCell(0);
    noPostLink.innerHTML = '<a href=\'/state/all\'>No marijuana sales here now. Checkout other sales around the US</a>';
    noPostLink.setAttribute('class','col-xs-12 col-sm-12 col-md-12 col-lg-12');
  }
}

function initializeLocalBusinesses(localBusinesses) {
  if (localBusinesses === null || localBusinesses === undefined) {
    return;
  }

  var table = document.getElementById('table_body_local_business');
  for(var i = table.rows.length - 1; i >= 0; --i)
  {
      table.deleteRow(i);
  }

  var totalLocalBusinesses = 0;

  // Chrome, safara and ie has different order or json array
  var startIndex = 0;
  var endIndex = localBusinesses.length;
  var step = 1;
  if (localBusinesses.length > 1) {
    if (localBusinesses[localBusinesses.length-1]['rating'] < localBusinesses[0]['rating']) {
      startIndex = localBusinesses.length - 1;
      endIndex = -1;
      step = -1;
    }
  }

  for (var i = startIndex; i !== endIndex; i += step)
  {
    if (stateFilter != null && localBusinesses[i]['state'] != stateFilter) {
      console.log("here1");
      continue;
    }

    if (!localBusinesses[i]['city']) {
      console.log("there");
      continue;
    }

    lat = localBusinesses[i]['lat'];
    lng = localBusinesses[i]['lng'];

    var postingLocation = new google.maps.LatLng(lat, lng);

    if (mapBound !== undefined && !mapBound.contains(postingLocation)) {
      continue;
    }

    var row = table.insertRow(table.length);
    var index = 0;

    // Address cell
    var dateLocationPosted = row.insertCell(index++);
    dateLocationPosted.innerHTML = localBusinesses[i]['address'];
    dateLocationPosted.setAttribute('class','col-xs-6 col-sm-6 col-md-4 col-lg-4');

    // Title cell
    var title = row.insertCell(index++);
    title.innerHTML = '<strong>' + localBusinesses[i]['title'] + '</strong>';
    title.setAttribute('class','col-xs-6 col-sm-6 col-md-4 col-lg-4');

    // Code for query with grouping
    // Quantity cell
    var rating = row.insertCell(index++);
    if (localBusinesses[i]['rating'] === null || localBusinesses[i]['rating'] === -1) {
      rating.innerHTML = "No rating";
    } else {
      rating.innerHTML = localBusinesses[i]['rating'];
    }
    rating.setAttribute('class','hidden-xs hidden-sm col-md-2 col-lg-2 text-center');

    // Phone number cell
    var title = row.insertCell(index++);
    if (localBusinesses[i]['phone_number'] === null || localBusinesses[i]['phone_number'] === -1) {
      title.innerHTML = "No number";
    } else {
      title.innerHTML = localBusinesses[i]['phone_number'];
    }
    title.setAttribute('class','hidden-xs hidden-sm col-md-2 col-lg-2');

    ++totalLocalBusinesses;
  }

  if (totalLocalBusinesses === 0) {
    var row = table.insertRow(table.length);
    var noLocalBusinessLink = row.insertCell(0);
    noLocalBusinessLink.innerHTML = '<a href=\'/state/all\'>No marijuana business here now. Checkout other cannabis businesses around the US</a>';
    noLocalBusinessLink.setAttribute('class','col-xs-12 col-sm-12 col-md-12 col-lg-12');
  }
}

function newXMLRequest(func, cacheEntry, extraParams) {
  var xmlhttp;

  // code for IE7+, Firefox, Chrome, Opera, Safari
  if (window.XMLHttpRequest)
  {
    xmlhttp=new XMLHttpRequest();
  }
  // code for IE6, IE5
  else
  {
    xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.onreadystatechange=function()
  {
    if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
      var docs = JSON.parse(xmlhttp.responseText);
      // Store response
      if (cacheEntry !== undefined) {
        cache[cacheEntry] = docs;
      }

      // Invoke callback function
      if (func != null) {
        func(cache, extraParams);
      }
    }
  }

  return xmlhttp;
}

function loadData() {
  // geocoder = new google.maps.Geocoder();
  navigator.geolocation.getCurrentPosition(function(pos){
    curLatLng = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
    handleMapChange();
    // codeLatLng(pos.coords.latitude, pos.coords.longitude);
  });

  // Postings xml request
  var params = location.pathname.split('/');
  var postingUrl = "/postings/";
  var localBusinessUrl = "/localBusinesses/";
  var priceUrl = "/prices/";
  if (params.length > 2 && params[1] === 'state') {
    postingUrl += params[2];
    localBusinessUrl += params[2];
    priceUrl += params[2];
    state = params[2]
  }

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'postings',
    [false, true, false, false]);
  xmlhttpPostings.open("POST", postingUrl, true);
  xmlhttpPostings.send();

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'localBusinesses',
    [false, true, false, false]);
  xmlhttpPostings.open("POST", localBusinessUrl, true);
  xmlhttpPostings.send();

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'prices',
    [false, false, true, false]);
  xmlhttpPostings.open("POST", priceUrl, true);
  xmlhttpPostings.send();
}