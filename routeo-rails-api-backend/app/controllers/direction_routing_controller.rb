
class DirectionRoutingController < ApplicationController
  def index
  	render json: {appInfo: "routeo", status: "success"}
  end

  def parse_loc
  	if params.has_key?("origin_lat") && params.has_key?("origin_long") && params.has_key?("dest_lat") && params.has_key?("dest_long") && params.has_key?("arrival_time")
		 	origin = ([params[:origin_lat].to_s,params[:origin_long].to_s]).join(",")
		 	destination = ([params[:dest_lat].to_s, params[:dest_long].to_s]).join(",")
		 	# sample: res = RestClient.get "https://maps.googleapis.com/maps/api/directions/json?origin=40.7533996,-73.9941375&destination=Queens&mode=transit&key=AIzaSyCbY6lVc7UgdBtmIc2Rkh2mBozbDyDWubo"
			res = RestClient.get "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&mode=transit&key=AIzaSyCbY6lVc7UgdBtmIc2Rkh2mBozbDyDWubo"
		
			obj = JSON.parse(res)

			total_time = obj["routes"].first["legs"].first["duration"]["text"]
			time_regex = /(\d)(\D*)(\d*)(\D*)/.match(total_time)
			if time_regex[2].empty?
				total_time = total_time.to_i
			else
				# puts time_regex[1].to_i
				# puts time_regex[3].to_i
				total_time = time_regex[1].to_i * 60 + time_regex[3].to_i
			end

			steps = obj["routes"].first["legs"].first["steps"]
			to_stop_walking_time = 0
			departure_stop = ""
			arrival_stop = ""
			steps.each do |step|
				if step["travel_mode"] == "TRANSIT"
					if step.has_key?("transit_details")
						departure_stop = step["transit_details"]["departure_stop"]["name"] 
						arrival_stop = step["transit_details"]["arrival_stop"]["name"] 
					end
					break
				else
					to_stop_walking_time += step["duration"]["text"].to_i
				end
			end
			# This is the time of the trip that excludes the time
			# it takes to walk to the first station
			remaining_trip_time = total_time - to_stop_walking_time
			# Arrive by 8am
			arrival_time = Time.parse(params[:arrival_time])

			# arrival_time = Time.new(arrival_time[0],arrival_time[1],arrival_time[2],arrival_time[3],arrival_time[4],0)
			# Find a train departure time that is earlier (smaller in datetime)
			# than this time; 3 minutes as buffer
			latest_train_departure_time = arrival_time - (remaining_trip_time) * 60
			departure_stop_id = get_stop_match(departure_stop)
			destination_stop_id = get_stop_match(arrival_stop)
			times = get_departure_table(departure_stop_id.to_i,destination_stop_id.to_i, latest_train_departure_time)
			# times = get_departure_table(603,604, Time.parse("07:33:00"))
			if times.empty?
				render json: {result: "No time found"}
			else
				results = Array.new
				times.each do |time|
					result = Hash.new
					# result["departure_time"] = departure_time
					result["subway_station_commute_time_mins"] = to_stop_walking_time
					result["train_name"] = time["line"]
					result["train_time"] = time["time"]
					result["subway_station_name"] = departure_stop
					results << result
				end

				render json: results
				# render json: {departure_stop: departure_stop, 
				# 							arrival_stop: arrival_stop, 
				# 							total_time: total_time, 
				# 							to_stop_walking_time: to_stop_walking_time, 
				# 							latest_train_departure_time: latest_train_departure_time, 
				# 							arrival_time: arrival_time,
				# 							departure_stop_id: departure_stop_id,
				# 							destination_stop_id: destination_stop_id

				# 						}
			end
			# render json: times
		else
			render json: { error: "ALL FIELDS REQUIRED"}
		end
  end

  def get_departure_table(departure_stop_id, destination_stop_id, latest_departure_time)
		
		res = RestClient.get "http://209.208.79.143:8080/?orig=" + departure_stop_id.to_s + "&dest=" + destination_stop_id.to_s
  	table = JSON.parse(res)

  	times = Array.new
  	table.each do |item|
  		time = Time.parse(item["time"])

  		if latest_departure_time < time
  			times << item
  		end
  	end
  	return times
  end

  def test_times
  	times = get_departure_table(603,604, Time.parse("07:33:00"))
  	render json: times
  end
	private  
  def get_stop_match(stop_name)
  	# stop_name = "penn station"
  	fz = FuzzyMatch.new(Stop.all, :read => :stop_name)
		stop = fz.find(stop_name)
		if stop.stop_parent.nil?
			return stop.stop_id
			# render json: {stop_id: stop.stop_id}
		else
			return stop.stop_parent
			# render json: {stop_id: stop.stop_parent}
		end
		# RestClient.get ""
  	
  end
end
