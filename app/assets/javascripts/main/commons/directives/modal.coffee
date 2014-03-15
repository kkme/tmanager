angular.module('directives.modal', [
  '$strap.directives'
])
.directive('modal', ['$q', '$modal', '$parse',($q, $modal, $parse)->
    restrict: 'A',
    scope: false,
    link: (scope, iElement, iAttrs) ->
      isOpening = false

      options = undefined
      scope.$watch(iAttrs.options, (newVal)->
        options = newVal
      , true) if(!!iAttrs.options)

      iElement.bind "click", ()->
        return if(isOpening is true)
        isOpening = true
        scope.$apply ->

        modalOptions = {}

        template = $parse(iAttrs.modal)(scope)
        newScope = scope.$new()
        newScope.options = options

        angular.forEach([
          'modalClass',
          'backdrop',
          'keyboard',
        ], (key) ->
          modalOptions[key] = scope.$eval(iAttrs[key]) if (angular.isDefined(iAttrs[key]))
        )
        angular.extend(modalOptions,
          template: template
          persist: false,
          show: false,
          scope: newScope
#          bodyEl : iElement
        )

        modalPromise = $modal(modalOptions)

        $q.when(modalPromise).then((modalEl)->
          modalEl.modal('show');
          newScope.$on '$routeChangeStart', ()->
            $(document.body).removeClass('modal-open')
            $(".modal-backdrop").remove()
            modalEl.remove()
          isOpening = false
        )
        modalPromise
  ])