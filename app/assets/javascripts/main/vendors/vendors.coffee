angular.module('vendors', [
  'ngRoute'
  'services.vendor'
]).config(['$routeProvider', 'PATH', ($routeProvider, PATH)->
  vendorService = ['VendorService', '$log', (VendorService, $log)->
    $log.debug "new Vendor.."
    new VendorService(true).promise
  ]

  $routeProvider.when("#{PATH.root}/:mvno/vendors",
    templateUrl: PATH.template + "/vendors/main.html"
    controller: 'VendorListCtrl'
    resolve:
      vendors: vendorService
  )
]).controller('VendorListCtrl', ['$scope', 'vendors', ($scope, vendors)->
  $scope.vendors = vendors

  $scope.searchOption = {}

  $scope.$watch('searchOption', (value)->
    if(value.telecom is null)
      delete $scope.searchOption.telecom
  , true)
])

.controller('VendorCreationCtrl', ['$route', '$scope', 'VendorService', ($route, $scope, VendorService)->
    $scope.vendors = new VendorService(false)

    $scope.original = if($scope.options and $scope.options.newVendor) then $scope.options.newVendor else
      telecom: 1

    $scope.clear = ->
      $scope.newVendor = angular.copy($scope.original)

    $scope.clear()
  ])
