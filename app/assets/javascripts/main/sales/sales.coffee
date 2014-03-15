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
        today = new Date()

        $log.debug "new Sale.."
        searchOption =
          start: new Date("#{today.getFullYear()}-#{today.getMonth() + 1}-01 00:00:00").getTime()

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
      today = new Date()
      $scope.start = $filter('date')(new Date("#{today.getFullYear()}-#{today.getMonth() + 1}-01 00:00:00"),
        'yyyy-MM-dd')
      $scope.startDate = $scope.start
      $scope.sales = new SaleService(false)
      $scope.shops = new ShopService(false)
      $scope.vendors = new VendorService(false)
      $scope.shops = new ShopService(false)
      $scope.colorManager = new ColorManager(false)

      $scope.changePeriod = ->
        option =
          start: new Date($scope.start).getTime()
        option.end = new Date($scope.end + " 23:59:59").getTime() if($scope.end)
        $scope.sales.list = []
        $scope.sales = new SaleService(true, option)
        $scope.startDate = $scope.start
        $scope.endDate = $scope.end

      $scope.searchOption =
        status: 2
      $scope.filteredList = ()->
        $filter('filter')($scope.sales.list, $scope.searchOption)

      $scope.getItem = (item)->
        $scope.sales.detail(item, (newItem)->
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

