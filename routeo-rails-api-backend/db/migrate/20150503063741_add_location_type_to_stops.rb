class AddLocationTypeToStops < ActiveRecord::Migration
  def change
    add_column :stops, :location_type, :string
  end
end
