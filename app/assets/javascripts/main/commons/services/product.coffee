angular.module('services.product',[
  'ngResource'
  'constants'
]).factory('Product',['$resource', 'PATH', ($resource, PATH)->
  $resource(PATH.api + "/products/:id",{id:'@id'},
    update:
      method:'PUT'
  )
]).factory('ProductService', ['Product', '$q', '$window', '$log', 'PATH', '$http', (Product, $q, $window, $log, PATH, $http)->
  beforeService = undefined
  (newable, options)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the product service retireves the list of products.
    Service.promise = future.promise

    #the list of products
    Service.list = []


    #update the list of products
    Service.updateList = (succ, error)->
      $log.debug "list"
      Product.query(options, (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()
      )

    #save a new item
    Service.save = (newItem, callback)->
      if(newItem.id)
        oldItem = Service.get(newItem)
        index = Service.list.indexOf(oldItem)
        Product.update(newItem, ->
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        Product.save newItem, (savedItem)->
          Service.list.push(savedItem)
          (callback or angular.noop)(savedItem)

    Service.remove = (item)->
      return if($window.confirm("정말 삭제 하시겠습니까?") == false)
      item.$remove(->
        index = Service.list.indexOf(item)
        Service.list.splice(index, 1)
      )

    Service.findById = (id)->
      for item in  Service.list
        if(item.id is id) then return item
      return undefined

    #retrieve an item
    Service.get = (data)->
      item = Service.findById(data.id)

      if(angular.isDefined(item))
        item
      else
        throw Error "Not Found : #{data.id}"

    Service.move = (shop, items, success, failure)->
      productIds = items.map (item)-> item.id
      $http.put("#{PATH.api}/products-move",
        shopId:shop.id
        products: productIds
      ).success (data, status, headers, config)->
        for id in productIds
          product = Service.findById(id)
          product.shop = shop.name if(!!product)
        (success or angular.noop)()
      .error (data, status, headers, config)->
        (failure or angular.noop)(status)

    Service.takeBack = (productId, reason, success, failure)->
      $http.post("#{PATH.api}/products/#{productId}/takeback",
        reason:reason
      ).success (data, status, headers, config)->
        Service.findById(productId).status = 3
        (success or angular.noop)()
      .error (data, status, headers, config)->
          (failure or angular.noop)(status)



    #Service initialization
    Service.updateList ->
      future.resolve Service
    , ->
      future.reject()
      throw Error "Cannot retrieve products"

    beforeService = Service
    Service
])