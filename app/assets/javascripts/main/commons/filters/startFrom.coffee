angular.module('filters.startFrom',[])
.filter('startFrom', ->
  (input, start) ->
    start = +(start*50) #parse to int
    input.slice(start)
)