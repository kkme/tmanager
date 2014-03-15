angular.module('directives.contenteditable',[])
  .directive('ngContentEditable',['$parse', ($parse)->
    #restrict to an attribute type.
    restrict: 'A'

    #element must have ng-model attribute.
    require: '^ngModel'

    link:(scope, elem, attr, ctrl)->
      bool = $parse(attr.ngContentEditable)(scope)
      elem.attr('contentEditable',bool)

      data = $parse(attr.ngModel)(scope)
      ctrl.$setViewValue(data)

      scope.$watch(attr.ngContentEditable, (newValue, oldValue)->
        if newValue isnt oldValue
          bool = newValue
          elem.attr('contentEditable',bool)
          elem.focus()
      )

      elem.on 'click', (ev)->
        if(bool)
          ev.preventDefault()
          false

      #view -> model
      elem.on 'blur', ->
        text = elem.text()

        if(text is "")
          ctrl.$render()
        else
          scope.$apply ->
            ctrl.$setViewValue text

      elem.on 'keydown', (ev)->
        switch ev.keyCode
          when 13  #Enter
            ev.preventDefault()
            elem.blur() #save the value
          when 27 #ESC
            ev.preventDefault()
            ctrl.$render() #recover the value
            elem.blur()

      #model -> view
      ctrl.$render = ->
        elem.text(ctrl.$viewValue or '')

      ctrl.$render()
  ])