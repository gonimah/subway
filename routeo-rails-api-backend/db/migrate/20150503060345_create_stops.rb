class CreateStops < ActiveRecord::Migration
  def change
    create_table :stops do |t|
      t.string :stop_id
      t.string :stop_name
      t.timestamps null: false
    end
  end
end
