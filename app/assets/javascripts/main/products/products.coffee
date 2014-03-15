angular.module('products', [
  'ngRoute'
  'services.product'
  'services.model'
  'services.vendor'
  'services.sale'
  'services.shop'
  'products.tools'
  'ngResource'
  'constants'
]).config([
  '$routeProvider',
  'PATH',
  ($routeProvider, PATH) ->
    todayProducts = [
      '$q',
      'ProductService',
      '$log',
      '$route',
      'ModelService',
      'VendorService',
      'ShopService',
      ($q, ProductService, $log, $route, ModelService, VendorService, ShopService)->
        today = new Date()
        $log.debug "new Product.."
        #        isMvno = $route.current.params.mvno is "mvno"
        $log.debug $route
        searchOption =
          startDate: new Date("#{today.toDateString()} 00:00:00").getTime()

        $q.all [
          new ProductService(true, searchOption).promise
          new ModelService(true).promise
          new VendorService(true).promise
          new ShopService(true).promise
        ]
    ]
    services = [
      '$q',
      'ProductService',
      '$log',
      '$route',
      'ModelService',
      'VendorService',
      'ShopService',
      ($q, ProductService, $log, $route, ModelService, VendorService, ShopService)->
        searchOption =
          status: 1
        $q.all [
          new ProductService(true, searchOption).promise
          new ModelService(true).promise
          new VendorService(true).promise
          new ShopService(true).promise
        ]
    ]


    $routeProvider.when("#{PATH.root}/:mvno/products-input",
      templateUrl: PATH.template + "/products/add/main.html"
      controller: 'ProductCreationCtrl'
      resolve:
        services: todayProducts
    ).when("#{PATH.root}/:mvno/products-move",
      templateUrl: PATH.template + "/products/move/move.html"
      controller: 'ProductMoveCtrl'
      resolve:
        services: services
    ).when("#{PATH.root}/:mvno/products-trace",
      templateUrl: PATH.template + "/products/trace.html"
      controller: 'TracingProductCtrl'
      resolve:
        services: services
    ).when("#{PATH.root}/:mvno/products",
      templateUrl: PATH.template + "/products/main.html"
      controller: 'ProductListCtrl'
      resolve:
        services: services
    )
])
.controller('ProductListCtrl', [
    '$scope',
    'ProductService',
    'ModelService',
    'ShopService',
    '$filter',
    'Sale',
    '$window',
    ($scope, ProductService, ModelService, ShopService, $filter, Sale, $window)->
      $scope.products = new ProductService(false)
      $scope.shops = new ShopService(false)

      $scope.saleProduct = (product, item, succ)->
        item.buyerName = newItem.buyerName.replace(/\s/g, "")
        return $window.alert("이름을 정확히 입력해 주세요.") if(angular.isArray(item.buyerName.match(/\*/)) or item.buyerName.length == 0)
        phone = item.phone or ""
        tail = phone
        head = ""
        if(phone.length > 3)
          tail = phone.slice(phone.length - 4, phone.length)
          head = phone.slice(0, phone.length - 4)

        savingItem =
          productId: product.id
          phoneNumberTail: tail
          phoneNumberHead: head
          memo: item.memo or ""
          ssna: item.ssna or ""
          buyerName: item.buyerName
          exTelecom: (item.exTelecom or "").toUpperCase()


        Sale.save savingItem, ()->
          (succ or angular.noop)()

      $scope.searchOption =
        status: 1

      $scope.filtered = ()->
        $filter('filter')($scope.products.list, $scope.searchOption)
  ])


.controller('ProductCreationCtrl', [
    '$scope',
    'ProductService',
    'ModelService',
    'VendorService',
    'ModelColorService',
    'ShopService',
    'ColorManager',
    'STATUS',
    ( $scope, ProductService, ModelService, VendorService, ModelColorService, ShopService, ColorManager, STATUS)->
      $scope.products = new ProductService(false)
      $scope.models = new ModelService(false)
      $scope.vendors = new VendorService(false)
      $scope.shops = new ShopService(false)
      $scope.colorManager = new ColorManager(false)
      $scope.STATUS = STATUS

      $scope.original = if($scope.options and $scope.options.newProduct) then $scope.options.newProduct else
        {}
      #      isOnlyMvno: isMvno

      $scope.clearSerialNumber = ()->
        $scope.newProduct.serialNumber = undefined
        delete $scope.newProduct.serialNumber

      $scope.getDate = (date)->
        if(angular.isString(date))
          date
        else
          new Date(date).toLocaleDateString()

      $scope.clear = (isSerialNumberOnly)->
        $scope.newProduct = angular.copy($scope.original)
        if($scope.newProduct.modelId)
          $scope.modelName = $scope.models.findById($scope.newProduct.modelId).name
        else $scope.modelName = ""


      $scope.modelList = $scope.models.list.map (item)->
        item.name

      $scope.$watch('modelName', (value)->
        for item in $scope.models.list
          if(item.name is value)
            $scope.newProduct.model = item
            $scope.newProduct.modelId = item.id
            return $scope.modelColors = new ModelColorService(item)
      )
      $scope.clear()
  ])

.controller('ProductMoveCtrl', [
    '$scope',
    'ProductService',
    'ShopService',
    '$filter',
    '$window',
    '$route',
    '$location',
    ($scope, ProductService, ShopService, $filter, $window, $route, $location)->
      $scope.products = new ProductService(false)
      $scope.shops = new ShopService(false)

      $scope.move = (item)->
        $scope.moveList.push(item)
        index = $scope.products.list.indexOf(item)
        $scope.products.list.splice(index, 1)

      $scope.moveBack = (item)->
        $scope.products.list.push(item)
        index = $scope.moveList.indexOf(item)
        $scope.moveList.splice(index, 1)

      $scope.save = ()->
        $scope.products.move($scope.target, $scope.moveList, ->
          $location.path("/app/#{$scope.mvno}/products")
        , ->
          $window.alert('에러 발생?')
        )

      $scope.$watch('targetName', (value)->
        for item in $scope.shops.list
          if(item.name is value)
            return $scope.target = item
        $scope.target = undefined
      )
      $scope.moveList = []

      $scope.searchOption =
        status: 1

      $scope.filtered = ()->
        $filter('filter')($scope.products.list, $scope.searchOption)
  ])
.factory('TraceList', [
    '$resource', 'PATH', ($resource, PATH)->
      $resource(PATH.api + "/products-trace")
  ])
.factory('Logs', ['$resource', 'PATH', ($resource, PATH)->
    $resource(PATH.api + "/products/:id/logs", {id: '@id'})
  ])
.directive('panelGroup', ()->
    restrict: 'C'
    link: (scope, elem, attr)->
      elem.on('show.bs.collapse', '.collapse', ()->
        id = $(this).attr('id')
        pattern = /^collapse\-(\d+)$/g
        result = pattern.exec(id)
        if(result.length == 2)
          scope.$emit('loading', result[1])
      )
  )
.controller('TracingProductCtrl', [
    '$scope',
    'TraceList',
    'ModelService',
    'VendorService',
    'STATUS',
    'Logs',
    ($scope, TraceList, ModelService, VendorService, STATUS, Logs)->
      $scope.models = new ModelService(false)
      $scope.vendors = new VendorService(false)
      $scope.STATUS = STATUS
      $scope.productLogs = {}

      $scope.traceList = (option)->
        $scope.list = []
        TraceList.query option, (list)->
          $scope.list = list

      $scope.getColor = (number)->
        switch number
          when 1 then "panel-info"
          when 2 then "panel-success"
          when 3 then "panel-danger"

      $scope.$on('loading', (ev, productId)->
        Logs.query({id: productId}, (list)->
          $scope.productLogs[productId] = list
        )
      )
      $scope.$watch('modelName', (value)->
        for item in $scope.models.list
          if(item.name is value)
            $scope.searchOption.model = item
            return $scope.searchOption.modelId = item.id
      )
  ])
