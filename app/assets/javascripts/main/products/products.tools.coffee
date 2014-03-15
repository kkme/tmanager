angular.module('products.tools', [

]).service('pToolService', ()->
  Service = {}
  Service.open = (tr, func)->
    Service.close()
    Service.list.push(func)
  Service.list = []
  Service.close = () ->
    angular.forEach(Service.list, (func)->
      (func or angular.noop)()
    )
    Service.list = []

  $(document).on('click', Service.close)
  Service
)
.directive('productTool', [
    'pToolService',
    '$compile',
    '$q',
    '$templateCache',
    '$timeout',
    '$http',
    (pToolService, $compile, $q, $templateCache, $timeout, $http)->
      restrict: 'A'
      scope: true
      link: (scope, elem, attr)->
        tr = $(elem.parents()[2])
        alreadyOpened = false
        trColor = undefined
        templateUrl = "/assets/templates/#{attr.productTool}.html"

        scope.openTool = (color)->
          if(!alreadyOpened)
            trColor = color
            pToolService.open(tr, scope.close)
            tr.animate('height': '250px')
            tr.addClass(color)
            alreadyOpened = true
            $q.when($templateCache.get(templateUrl) or $http.get(templateUrl, { cache: true }).then (res)->
              res.data
            ).then (template)->
              $tool = $('<div id="product-tool"></div>').html(template)
              $tool.css(
                'position': 'absolute'
                'margin-top': '20px'
                left: '10px'
                right: '10px'
              )
              $timeout ->
                $compile($tool)(scope)
                tr.find("td:first").append($tool)
          undefined


        scope.close = ()->
          tr.css('height', 'auto')
          tr.removeClass(trColor)
          tr.find("#product-tool").remove()
          alreadyOpened = false

        tr.click (ev)->
          ev.stopPropagation()
  ])