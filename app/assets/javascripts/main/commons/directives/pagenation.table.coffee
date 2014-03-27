angular.module('directives.pagenationtable',[
  'constants.path'
])
.directive('pageTable',['PATH','$parse', (PATH, $parse)->
    restrict : 'A'
    transclude : true
    replace : true
    templateUrl : PATH.inventoryTemplate + "/directives/pagenation.table.html"
    link :(scope, elem, attr)->
      list = $parse(attr.pageTable)(scope)
      scope.$watchCollection(attr.pageTable, (col)->
        scope.currentPage = 0
        list = col
        scope.pages = [0..Math.floor(list.length/50)]
      )
      scope.$watch('searchOption', ()->
        scope.currentPage = 0
      ,true )
  ])