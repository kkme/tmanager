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
        $log.debug "new Takeback.."
        startDate = new Date()
        startDate.setDate(1)
        startDate.setHours(0,0,0,0)
        searchOption =
          start: startDate.getTime()

        $q.all [
          new TakebackService(true, searchOption).promise
          new VendorService(true).promise
          new ShopService(true).promise
        ]
    ]

    $routeProvider.when("#{PATH.root}/:mvno/takebacks",
      templateUrl: PATH.inventoryTemplate + "/takebacks/main.html"
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
      startDate = new Date()
      startDate.setDate(1)
      startDate.setHours(0,0,0,0)
      $scope.start = $filter('date')(startDate, 'yyyy-MM-dd')
      $scope.startDate = $scope.start

      $scope.takebacks = new TakebackService(false)
      $scope.shops = new ShopService(false)
      $scope.vendors = new VendorService(false)

      $scope.changePeriod = ->
        $scope.takebacks.list = []
        option =
          start : $scope.changeStringToDateTime($scope.start, true)
        if($scope.end)
          option.end = $scope.changeStringToDateTime($scope.end, false)
        $scope.takebacks = new TakebackService(true, option)
        $scope.startDate = $scope.start
        $scope.endDate = $scope.end

      $scope.changeStringToDateTime = (string, begin)->
        return undefined if(!string)
        if(begin)
          start = new Date(string)
          start.setHours(0,0,0,0)
          start.getTime()
        else
          end = new Date(string)
          end.setHours(23,59,59,999)
          end.getTime()
      $scope.searchOption =
        status: 3

      $scope.filtered = ()->
        $filter('filter')($scope.takebacks.list, $scope.searchOption)
])

