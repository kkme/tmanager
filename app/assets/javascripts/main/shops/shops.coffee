angular.module('shops', [
  'ngRoute'
  'services.shop'
]).config(['$routeProvider', 'PATH', ($routeProvider, PATH)->
  shopService = ['ShopService', '$log', (ShopService, $log)->
    $log.debug "new Shop.."
    new ShopService(true).promise
  ]

  $routeProvider.when("#{PATH.root}/:mvno/shops",
    templateUrl: PATH.inventoryTemplate + "/shops/main.html"
    controller: 'ShopListCtrl'
    resolve:
      shops: shopService
  )
]).controller('ShopListCtrl', ['$scope', 'shops', ($scope, shops)->
  $scope.shops = shops

  $scope.searchOption = {}
])

.controller('ShopCreationCtrl', [
    '$route',
    '$scope',
    'ShopService',
    ($route, $scope, ShopService)->
      $scope.shops = new ShopService(false)

      $scope.original = if($scope.options and $scope.options.newShop) then $scope.options.newShop else {}

      $scope.clear = ->
        $scope.newShop = angular.copy($scope.original)

      $scope.clear()
  ])
