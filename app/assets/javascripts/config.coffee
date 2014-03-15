require([
  'main/app'
  'main/commons/constants'
  'main/commons/directives/modal'
  'main/commons/directives/content.editable'
  'main/commons/directives/ng.enter'
  'main/commons/directives/typeahead'
  'main/commons/directives/scrollToLoad'
  'main/commons/services/model'
  'main/commons/services/modelColor'
  'main/commons/services/product'
  'main/commons/services/sale'
  'main/commons/services/shop'
  'main/commons/services/takeback'
  'main/commons/services/vendor'
  'main/models/models'
  'main/products/products'
  'main/products/products.tools'
  'main/sales/sales'
  'main/shops/shops'
  'main/takebacks/takebacks'
  'main/vendors/vendors'
], () ->
  window.location.hash = "" if window.location.hash isnt ""

  angular.element(document).ready ->
    angular.bootstrap document, ['app']
)

