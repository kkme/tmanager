angular.module('app', [
  'constants' #constants.path
  '$strap.directives'
  'directives.modal'
  'directives.contenteditable'
  'directives.ngenter'
  'directives.pagenationtable'
  'directives.typeahead'
  'filters.startFrom'
  'filters.buyer'
  'models'
  'products'
  'sales'
  'shops'
  'takebacks'
  'vendors'
]).config([
  '$routeProvider',
  '$locationProvider',
  '$httpProvider',
  '$logProvider',
  'PATH',
  ($routeProvider, $locationProvider, $httpProvider, $logProvider, PATH)->
    $logProvider.debugEnabled true
    #TODO - html5 모드를 true로 할경우 win7 ie8 ie9에서 infinite reloading 발생.
    $locationProvider.html5Mode(true)
    $routeProvider.when("#{PATH.root}/main",
      templateUrl: PATH.template + "/main.html"
    ).otherwise(redirectTo: "#{PATH.root}/main")

    $httpProvider.interceptors.push ['$q', '$log', '$window', '$rootScope', ($q, $log, $window, $rootScope)->
      request: (config)->
        #MVNO에 대해서 전체 설정
        config.params = config.params || {}
        if(angular.isString(config.url) and !config.url.match(/\.html$/g))
          config.params.mvno = $rootScope.mvno is "mvno"
          config.headers['cache-control'] ='no-cache'
        config || $q.when(config)
      responseError: (response)->
        switch response.status
          when 400
            $window.alert '잘못된 요청입니다.'
            $log.debug response
          when 401
            $window.alert '세션이 종료되었습니다.'
            $window.location = '/logout'
          when 403
            $log.debug(response)
            $window.alert response.data
          when 500
            $window.alert '서버 오류가 발생하였습니다. 다시 시도해 보세요.'
        $q.reject(response)
    ]
]).run ['$rootScope', '$location', '$window', ($rootScope, $location, $window)->
  $rootScope.$on('$routeChangeStart', ->
    $('#loader').show()
    $('article').hide()
    $rootScope.mvno = if($location.path().match(/mvno/)) then  "mvno" else "normal"
  )
  $rootScope.$on('$routeChangeSuccess', ->
    $('#loader').hide()
    $('article').show()
  )
  $rootScope.$on '$routeChangeError', ->
    $window.alert('페이지 로딩에 실패하였습니다.\n 페이지 새로 고침을 해주세요.')
  $rootScope.$on 'modal-hide', ->
    $("body").removeClass('modal-open')
    $(".modal-backdrop").remove()
]
