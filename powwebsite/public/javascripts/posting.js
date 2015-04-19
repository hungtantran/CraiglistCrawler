// Function that reupdate the display with the newest filter
function updateDisplay(displayInfo, initializeParams) {
  if (displayInfo === undefined) {
    displayInfo = cache;
  }

  var isInitializeMap = true;
  var isInitializePrice = true;
  if (initializeParams !== undefined) {
    isInitializeMap = initializeParams[0];
    isInitializePrice = initializeParams[1];
  }

  if (isInitializeMap === undefined || isInitializeMap) {
    initializeMap(displayInfo['postings']);
  }

  if (isInitializePrice === undefined || isInitializePrice) {
    initializePrices(displayInfo['prices']);
  }
}

function initializePostingBodyContent() {
  var content = $("#postingBodyContent").html();
  
  var elements = $(content).highlightRegex('[\d+/\d+oz|\d+/\d+ ounce|\d+oz|\d ounces|eighth|eighths|quarter|quarter oz|quart|quarters|half|half oz|ounce|oz|1/8th|1/8TH|8ths|8THS|HALF|FULL|an o|a qp|a hp|an hp|a unit|a pound|\d+/\d+ | \d+[0-9]*\.?[0-9]* grams | \d+[0-9]*\.?[0-9]*grams | gram | GRAMS | \d+[0-9]*\.?[0-9]*gram | \d+[0-9]*\.?[0-9]*GRAMS | \d+[0-9]*\.?[0-9]*g | \d+[0-9]*\.?[0-9]*G]|\d+/\d+|\d+?[0-9]*\.?[0-9]+ dollar|\d+?[0-9]*\.?[0-9]+ donation|\\$\d+?[0-9]*\.?[0-9]+|\\$+?[0-9]*\.?[0-9]+|[0-9]*\.?[0-9]+');
  var postingBody = $('#postingbody', elements);

  $('#postingBodyContent').empty();
  $('#postingBodyContent').append(postingBody);
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
    if (isPostingPage && prices[i]['price_fk'] == id) {
      postingPrices.push(prices[i]);
    }

    lat = prices[i]['lat'];
    lng = prices[i]['lng'];

    var priceLocation = new google.maps.LatLng(lat, lng)
    newPrices.push(prices[i]);
  }

  newPriceBin('price_bin_dist_by_state', newPrices);
}

function newPriceBin(divId, prices) {
  var binNames = ['0-5', '5-10', '10-15', '15-20', '20-25', '25-30', '30-35', '35-40','40-45','45+'];
  data = {}
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

  arr = [];
  for (k in data) {
    data[k]['state'] = k;
    data[k]['bins'] = binNames.map( function(name) { return {name:name, value:data[k][name], state:k} });

    arr.push(data[k]);
  }

  var margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = $('#' + divId).width()- margin.left - margin.right,
    height = 250*arr.length;//250 - margin.top - margin.bottom;
  
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

  state.append("g")
    .attr("class", "y axis")
    .attr("transform", "translate(0,0)")
    .call(yAxisBins);

  state.selectAll("rect")
    .data(function(d) {
      return d['bins']; })
  .enter().append("text")
    .text(function(d) { 
      if (d.value == null) {
        return 0;
      } else {
        if (d.value == 0) return "";
        return d.value;
      }
    })
    .attr("x", function(d) {
      if (d.value == null) {
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
        if (d.value == null) {
          return 0;
        } else {
          return x(d.value);
        }
      })
      .attr("x", 0)
      .attr("y", function(d) {
        return y1(d.name)+2;
      })
      .attr("height", y1.rangeBand()-4)
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
        if (stateFilter == d.state) stateFilter = null;
        else stateFilter = d.state;
        updateDisplay();
        })
      .style("fill", function(d) {
        if (stateFilter == null || d.state == stateFilter) {
          return color(d.name);
        } else {
          return "#F5F5F5";
        }
        });
}

// Callback function to handle change in map
function handleMapChange() {
  updateDisplay();
}

// Function that draw marker on map
function drawMarker(map, markers) {
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
      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, lng),
        map: map
      });

      google.maps.event.addListener(marker, 'click', handleMapChange);

      markerArray.push(marker);
    }
  }
}

// Reinitialize the map, call to redraw the map
function initializeMap(markers, redrawMap) {
  // Initialize maps
  if (map == null || (redrawMap != null && redrawMap == true)) {
    if (typeof lat_ !== 'undefined' && typeof long_ !== 'undefined' && lat_ != null && long_ != null) {
      map = newMap(parseFloat(lat_), parseFloat(long_), 7, 'map-canvas', false);
    } else if (typeof state_ !== 'undefined' && typeof city_ !== 'undefined' && state_ != null && city_ != null) {
      geocoder.geocode( { 'address': city_ + ", " + state_}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
          map = newMap(results[0].geometry.location.lat(), results[0].geometry.location.lng(), 5, 'map-canvas');
        } else {
          map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
        }
        google.maps.event.addListener(map, 'idle', handleMapChange);
      });
    } else {
      map = newMap(42.2030543,-98.602256, 4, 'map-canvas');
    }

    google.maps.event.addListener(map, 'idle', handleMapChange);
  }

  // Initialize markers
  if (map != null && markers != null) {
    drawMarker(map, markers);
  }

  return map;
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
  geocoder = new google.maps.Geocoder();

  // Postings xml request
  var params = location.pathname.split('/');
  var postUrl = "/postings/";
  var priceUrl = "/prices/";

  postUrl += params[2];
  priceUrl += params[2];

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'postings',
    [true, false]);
  xmlhttpPostings.open("POST", postUrl, true);
  xmlhttpPostings.send();

  var xmlhttpPostings = new newXMLRequest(
    updateDisplay,
    'prices',
    [false, true]);
  xmlhttpPostings.open("POST", priceUrl, true);
  xmlhttpPostings.send();
}