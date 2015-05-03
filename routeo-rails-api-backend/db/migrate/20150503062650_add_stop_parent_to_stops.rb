class AddStopParentToStops < ActiveRecord::Migration
  def change
    add_column :stops, :stop_parent, :string
  end
end
