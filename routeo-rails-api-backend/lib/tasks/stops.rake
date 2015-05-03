require 'csv'
namespace :stops do
  task :import_stop_data  => :environment do
    CSV.foreach(Rails.root + 'csv/stop-info.csv') do |row|
			if $INPUT_LINE_NUMBER > 1 
				@stop = Stop.new
				@stop.stop_id = row[0]
				# && $INPUT_LINE_NUMBER < 10
				@stop.stop_name = row[2]
				@stop.location_type = row[8]
				@stop.stop_parent = row[9]
				@stop.save
				# puts "stop_id: #{@stop.stop_id}, stop_name: #{@stop.stop_name}, stop_parent: #{@stop.stop_parent}"
			end
		end
  end
 
end
