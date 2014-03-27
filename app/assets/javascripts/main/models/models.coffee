###
  Model은 Mvno가 사실상 의미가 없다.
###

angular.module('models', [
  'ngRoute'
  'services.model'
  'services.modelcolor'
]).config(['$routeProvider', 'PATH', ($routeProvider, PATH)->
  modelService = ['ModelService', '$log', (ModelService, $log)->
    $log.debug "new Model.."
    new ModelService(true).promise
  ]

  $routeProvider.when("#{PATH.root}/:mvno/models",
    templateUrl: PATH.inventoryTemplate + "/models/main.html"
    controller: 'ModelListCtrl'
    resolve:
      models: modelService
  )
]).controller('ModelListCtrl', ['$scope', 'models', ($scope, models)->
  $scope.models = models

  $scope.searchOption = {}
])
# model service와  modelcolor service를 따로 둔다.
# model service와 modelcolor service는 orthogonal하게 만든다.


# 생성시(model id가 없을 때)에는 model post에 색상값을 실어 보내고,
# 수정시(model id가 존재할 때)에는 실시간으로 업데이트되게 만든다

#TODO - 수정시 색상 추가 삭제는 취소가 되지 않습니다. 신중히 하여주세요! 라는 문구를 넣을 것.
.controller('ModelCreationCtrl',
    ['$route', '$scope', 'ModelService', 'ModelColorService', ($route, $scope, ModelService, ModelColorService)->
      $scope.models = new ModelService(false)

      $scope.original = if($scope.options and $scope.options.newModel) then $scope.options.newModel else {}

      $scope.newColor = ""
      $scope.newModel = angular.copy($scope.original)
      $scope.modelColors = new ModelColorService($scope.newModel)

      $scope.save = (callback)->
        $scope.newModel.colors = $scope.modelColors.list
        $scope.models.save($scope.newModel, callback)

      $scope.addColor = ()->
        if($scope.newColor isnt "")
          $scope.modelColors.save(
            color: $scope.newColor
          , ->
            $scope.newColor = ""
          )

    ])


