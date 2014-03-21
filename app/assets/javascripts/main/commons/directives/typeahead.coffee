angular.module('directives.typeahead', [])
.directive('ngTypeahead', ['$parse', '$log',($parse, $log)->
    restrict: 'A'
    require: 'ng-model'
    link:
      pre: (scope, elem, attr, ngModel)->
        array = $parse(attr.local)(scope)
        if(!!attr.datumKey)
          valueKey = attr.datumKey
        else
          valueKey = 'value'
        #이름이 있으면 같은 local(array)를 재사용한다.
        name = $parse(attr.sparkTypeahead)(scope)
        return if !array
        createTypeahead = (option)->
          elem.typeahead(option)
          #typeahead가 생성되고 나면, span으로 감싸진다.
          #input의 효과를 span에 줘야한다.
          elem.parent().addClass(attr.class)

        createTypeahead
          name: name
          valueKey:valueKey
          local: array
          limit: 10
          minLength:0

        elem.on 'typeahead:closed typeahead:selected typeahead:autocompleted', (ev, val)->
          $log.debug val
          if !!val
            scope.$apply ->
              ngModel.$setViewValue val[valueKey]
              scope.$broadcast('typeahed-completed', val[valueKey])

        #When the scope is destroied, Typeahead should be destroied
        scope.$on '$destroy', ->
          elem.typeahead('destroy')

        #Typeahead doesn't support to update the list of local.
        scope.$watch(attr.local, (newValue, oldValue)->
          if(newValue isnt oldValue)
            $log.debug "[typeahead] local array is changed. so it will be recreated"
            elem.typeahead('destroy')
            createTypeahead
              name: name
              valueKey:valueKey
              local: newValue
              limit: 10
              minLength:0
        , true)


  ])