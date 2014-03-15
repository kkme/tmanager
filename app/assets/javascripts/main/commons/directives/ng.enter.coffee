angular.module('directives.ngenter',[])
  .directive('ngEnter', ->
    (scope,elem,attrs)->
      elem.bind("keydown keypress", (ev)->
        if(ev.which is 13)
          scope.$apply ->
            scope.$eval(attrs.ngEnter)

          ev.preventDefault()
      )
  )