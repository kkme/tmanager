angular.module('services.shop',[
  'ngResource'
  'constants'
]).factory('Shop',['$resource', 'PATH',($resource, PATH)->
  $resource(PATH.api + "/shops/:id",{id:'@id'},
    update:
      method:'PUT'
  )
]).factory('ShopService', ['Shop', '$q', '$window',(Shop, $q, $window)->
  beforeService = undefined

  (newable)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the shop service retireves the list of shops.
    Service.promise = future.promise

    #the list of shops
    Service.list = []


    #update the list of shops
    Service.updateList = (succ, error)->
      Shop.query (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()

    #save a new item
    Service.save = (newItem, callback)->
      newItem.email = "" if(!newItem.email)
      if(newItem.id)
        oldItem = Service.get(newItem)
        index = Service.list.indexOf(oldItem)
        Shop.update(newItem, ->
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        Shop.save newItem, (savedItem)->
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
    Service.findByName = (name)->
      for item in Service.list
        return item if(item.name is name)
      return undefined
    #retrieve an item
    Service.get = (data)->
      item = Service.findById(data.id)

      if(angular.isDefined(item))
        item
      else
        throw Error "Not Found : #{data.id}"


    #Service initialization
    Service.updateList ->
      future.resolve Service
    , ->
      future.reject()
      throw Error "Cannot retrieve shops"

    beforeService = Service
    Service
])