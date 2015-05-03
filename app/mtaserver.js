var GtfsRealtimeBindings = require('gtfs-realtime-bindings');
var request = require('request');
var http = require('http');
var url = require('url');

var PORT = 8080;

var server = http.createServer(function (req, resp) {

    var requestSettings = {
      method: 'GET',
      url: 'http://datamine.mta.info/mta_esi.php?key=<thekey>&feed_id=1', // use feed_id = 2 for L trains
      encoding: null
    };
    
    var url_parts = url.parse(req.url, true);
    //var line = url_parts.query.line;
    var orig = url_parts.query.orig;
    var dest = url_parts.query.dest;

    request(requestSettings, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            
            var feed = GtfsRealtimeBindings.FeedMessage.decode(body);
            var times = new Array();
            
            var orig_id = isNumber(orig);
            var dest_id = isNumber(dest);
            
            if(orig_id && dest_id)
            {
                var dir_idx = parseInt(orig) - parseInt(dest);
                var direction = 'S';
                
                //console.log('dir_idex: ' + dir_idx);
                
                if(dir_idx > 0) {
                    direction = 'N';
                }
                
                var station_code = orig + direction;
                
                console.log('direction: ' + direction);
                console.log('station_code: ' + station_code);
                
                feed.entity.forEach(function(entity) {

                    if (entity.trip_update) {
                        //console.log(entity.trip_update.trip.trip_id);

                        var id_split = entity.trip_update.trip.trip_id.split('_');
                        var line = id_split[1][0].split('.')[0];

                        var split_time_calc = (((id_split[0]) / 100) / 60);
                        var start_time_hour = Math.round(parseInt(split_time_calc), 0);
                        var start_time_minutes = Math.round((((id_split[0]) / 100) % 60), 0);

                        if(start_time_hour < 10)
                            start_time_hour = '0' + start_time_hour;

                        if(start_time_minutes < 10)
                            start_time_minutes = '0' + start_time_minutes;

                        entity.trip_update.stop_time_update.forEach(function(stoptime) {

                            if(stoptime.arrival) {

                                if(stoptime.stop_id == station_code) {

                                    var timestamp = stoptime.arrival.time.low;
                                    var pubDate = new Date(timestamp * 1000); 

                                    var hours = pubDate.getHours();
                                    var mins = pubDate.getMinutes();
                                    var seconds = pubDate.getSeconds();

                                    if(hours < 10)
                                        hours = '0' + hours;

                                    if(mins < 10)
                                        mins = '0' + mins;

                                    if(seconds < 10)
                                        seconds = '0' + seconds;

                                    times.push(new traintime(stoptime.stop_id, hours + ':' + mins + ':' + seconds, line));
                                }
                            }

                        });
                    }
                });
                
                resp.writeHead(200, { 'Content-Type': 'application/json' });
                resp.end(JSON.stringify(times));
            }
            else
            {
                resp.writeHead(200, { 'Content-Type': 'application/json' });
                resp.end('{ "error" : "NO LINE" }');
            }
        }
    });
});

function traintime(stop, time, line) {
    this.stop = stop;
    this.time = time;
    this.line = line;
}

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}
                               
server.listen(PORT);
