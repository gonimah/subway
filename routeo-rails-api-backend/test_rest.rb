require 'rubygems'
require 'rest-client'
require 'json'

res = RestClient.get "https://maps.googleapis.com/maps/api/directions/json?origin=40.7533996,-73.9941375&destination=Queens&mode=transit&key=AIzaSyCbY6lVc7UgdBtmIc2Rkh2mBozbDyDWubo"
obj = JSON.parse(res)

total_time = obj["routes"].first["legs"].first["duration"]["text"]
time_regex = /(\d)(\D*)(\d*)(\D*)/.match(total_time)
if time_regex[2].empty?
	total_time = total_time.to_i
else
	puts time_regex[1].to_i
	puts time_regex[3].to_i
	total_time = time_regex[1].to_i * 60 + time_regex[3].to_i
end

steps = obj["routes"].first["legs"].first["steps"]
to_stop_walking_time = 0
departure_stop = ""
steps.each do |step|
	if step["travel_mode"] == "TRANSIT"
		departure_stop = step["transit_details"]["departure_stop"]["name"] if step.has_key?("transit_details")
		break
	else
		to_stop_walking_time += step["duration"]["text"].to_i
	end
end

puts departure_stop
puts to_stop_walking_time
puts total_time

puts remaining_trip_time = total_time - to_stop_walking_time

# Arrive by 8am
arrival_time = Time.new(2015,5,1,8,0,0)
# Find a train departure time that is earlier (smaller in datetime) than this time
latest_train_departure = arrival_time - remaining_trip_time * 60
puts latest_train_departure

