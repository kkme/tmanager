angular.module('takebacks', [
  'ngRoute'
  'services.takeback'
  'services.model'
  'services.vendor'
  'services.sale'
  'services.shop'
]).config([
  '$routeProvider',
  'PATH',
  ($routeProvider, PATH) ->
    todayTakebacks = [
      '$q',
      'TakebackService',
      '$log',
      '$route',
      'ModelService',
      'VendorService',
      'ShopService',
      ($q, TakebackService, $log, $route, ModelService, VendorService, ShopService)->
        today = new Date()
        $log.debug "new Takeback.."
        searchOption =
          start: new Date("#{today.getFullYear()}-#{today.getMonth() + 1}-01 00:00:00").getTime()

        $q.all [
          new TakebackService(true, searchOption).promise
          new VendorService(true).promise
          new ShopService(true).promise
        ]
    ]

    $routeProvider.when("#{PATH.root}/:mvno/takebacks",
      templateUrl: PATH.template + "/takebacks/main.html"
      controller: 'TakebackListCtrl'
      resolve:
        services: todayTakebacks
    )
])

.controller('TakebackListCtrl', [
    '$scope',
    'TakebackService',
    'ShopService',
    'VendorService',
    '$filter',
    ($scope, TakebackService, ShopService, VendorService, $filter)->
      today = new Date()
      $scope.start = $filter('date')(new Date("#{today.getFullYear()}-#{today.getMonth() + 1}-01 00:00:00"),
        'yyyy-MM-dd')
      $scope.startDate = $scope.start

      $scope.takebacks = new TakebackService(false)
      $scope.shops = new ShopService(false)
      $scope.vendors = new VendorService(false)

      $scope.changePeriod = ->
        $scope.takebacks.list = []
        option =
          start: new Date($scope.start).getTime()
        option.end = new Date($scope.end + " 23:59:59").getTime() if($scope.end)
        $scope.takebacks = new TakebackService(true, option)
        $scope.startDate = $scope.start
        $scope.endDate = $scope.end

      $scope.searchOption =
        status: 3

      $scope.filtered = ()->
        $filter('filter')($scope.takebacks.list, $scope.searchOption)
])

