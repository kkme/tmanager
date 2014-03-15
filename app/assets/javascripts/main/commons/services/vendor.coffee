angular.module('services.vendor',[
  'ngResource'
  'constants'
]).factory('Vendor',['$resource', 'PATH', ($resource, PATH)->
  $resource(PATH.api + "/vendors/:id",{id:'@id'},
    update:
      method:'PUT'
  )
]).factory('VendorService', ['Vendor', '$q', '$window',(Vendor, $q, $window)->
  beforeService = undefined

  (newable)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the vendor service retireves the list of vendors.
    Service.promise = future.promise

    #the list of vendors
    Service.list = []

    Service.vendors = [
      name : 'SKT'
      value : 1
    ,
      name : 'KT'
      value:2
    ,
      name : 'LGT'
      value:3
    ]


    #update the list of vendors
    Service.updateList = (succ, error)->
      Vendor.query (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()

    #save a new item
    Service.save = (newItem, callback)->
      if(newItem.id)
        oldItem = Service.get(newItem)
        index = Service.list.indexOf(oldItem)
        Vendor.update(newItem, ->
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        Vendor.save newItem, (savedItem)->
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


    #Service initialization
    Service.updateList ->
      future.resolve Service
    , ->
      future.reject()
      throw Error "Cannot retrieve vendors"

    beforeService = Service
    Service
])