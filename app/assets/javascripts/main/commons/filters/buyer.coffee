angular.module('filters.buyer',[])
.filter('buyer', ()->
    (input) ->
      input.slice(0,1) + "*" + input.slice(2)
  )