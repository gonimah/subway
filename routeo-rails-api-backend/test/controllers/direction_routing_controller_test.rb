require 'test_helper'

class DirectionRoutingControllerTest < ActionController::TestCase
  test "should get index" do
    get :index
    assert_response :success
  end

end
