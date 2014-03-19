angular.module('sales', [
  'ngRoute'
  'services.sale'
  'services.model'
  'services.vendor'
  'services.sale'
  'services.shop'
]).config([
  '$routeProvider',
  'PATH',
  ($routeProvider, PATH) ->
    thisMonthSales = [
      '$q',
      'SaleService',
      '$log',
      '$route',
      'ModelService',
      'VendorService',
      'ShopService',
      ($q, SaleService, $log, $route, ModelService, VendorService, ShopService)->
        $log.debug "new Sale.."
        startDate = new Date()
        startDate.setDate(1)
        startDate.setHours(0,0,0,0)
        searchOption =
          start: startDate.getTime()

        $q.all [
          new SaleService(true, searchOption).promise
          new ModelService(true).promise
          new VendorService(true).promise
          new ShopService(true).promise
        ]
    ]
    $routeProvider.when("#{PATH.root}/:mvno/sales",
      templateUrl: PATH.template + "/sales/main.html"
      controller: 'SaleListCtrl'
      resolve:
        services: thisMonthSales
    ).when("#{PATH.root}/:mvno/sales-detail-search",
      templateUrl: PATH.template + "/sales/detail.html"
      controller: 'DetailSearchCtrl'
    )
])
.controller('SaleListCtrl', [
    '$scope',
    'SaleService',
    'ModelService',
    'ShopService',
    '$filter',
    'ColorManager',
    'VendorService',
    ($scope, SaleService, ModelService, ShopService, $filter, ColorManager, VendorService)->
      startDate = new Date()
      startDate.setDate(1)
      startDate.setHours(0,0,0,0)
      $scope.startDate = $filter('date')( startDate, 'yyyy-MM-dd')
      $scope.sales = new SaleService(false)
      $scope.shops = new ShopService(false)
      $scope.vendors = new VendorService(false)
      $scope.shops = new ShopService(false)
      $scope.colorManager = new ColorManager(false)

      $scope.changePeriod = ->
        option = {}
        option.start = new Date($scope.start).getTime() if($scope.start)
        if($scope.end)
          end = new Date($scope.end)
          end.setHours(23,59,59,999)
          option.end = (end).getTime()
        option.name = $scope.name if($scope.name)

        for item of option #if option has property
          $scope.sales.list = []
          $scope.sales = new SaleService(true, option)
          $scope.startDate = $scope.start
          $scope.endDate = $scope.end
          $scope.buyerName = $scope.name
          return


      $scope.searchOption =
        status: 2

      $scope.filteredList = ()->
        filtered = $filter('filter')($scope.sales.list, $scope.searchOption)
        if(!!$scope.search_shopId)
          $filter('filter')(filtered, {shopId:$scope.search_shopId}, true)
        else
          filtered
      $scope.changeStringToDateTime = (string, begin)->
        return undefined if(!string)
        if(begin)
          new Date(string).getTime()
        else
          new Date(string + " 23:59:59").getTime()

      $scope.getItem = (item)->
        $scope.sales.detail(item, (newItem)->
          window.newItem = newItem
          console.log newItem
          $scope.newItem = angular.copy(newItem)
          item)
  ])

.controller('DetailSearchCtrl', [
    '$scope',
    '$http',
    'PATH',
    '$log',
    ($scope, $http, PATH, $log)->
      $scope.search = (item)->
        $http.get(PATH.api + '/sales-detail',
          params: item
        ).then (item)->
          $log.debug item.data
          $scope.list = item.data
  ])

