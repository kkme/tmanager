angular.module('directive.scrollToLoad', [])
#app 전체에서 한번만 binding 걸리도록 service로 등록.
  .service('loadMore', ['$window', '$rootScope', ($window, $rootScope)->
    $(window).scroll(() ->
      if ($(document).height() == $(window).scrollTop() + $(window).height())
        $rootScope.$broadcast("loadMore", undefined)
    )
    @
  ])
#http://jsfiddle.net/vojtajina/U7Bz9/
# June 12, 2013 14:49 GMT + 09:00

  .directive('whenScrolled', ['loadMore', (loadMore)->
    (scope, elm, attr) ->
#    raw = elm[0]
#    elm.bind 'scroll', ->
#      scope.$apply(attr.whenScrolled) if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight)
      scope.$on("loadMore", ->
        scope.$apply(attr.whenScrolled) if !!attr.whenScrolled
      )
  ])

  .directive('scrollTop', ()->
    restrict: 'A'
    scope:true
    link: (scope, elem, attr)->
      scrollTop = ()->
        $("body").animate({scrollTop: 0}, 500)

      scope.$on('$routeChangeSuccess', scrollTop)
      elem.bind 'click', scrollTop

      $(window).scroll(()->
        show()
      )

      show = ()->
        if($(window).scrollTop() < 10)
          elem.fadeOut()
        else
          elem.fadeIn()
      show()
  )
