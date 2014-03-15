angular.module('services.takeback',[
  'ngResource'
  'constants'
]).factory('Takeback',['$resource', 'PATH',($resource, PATH)->
  $resource(PATH.api + "/takebacks/:id",{id:'@id'},
    update:
      method:'PUT'
  )
]).factory('TakebackService', ['Takeback', '$q', '$window', '$log', (Takeback, $q, $window, $log)->
  beforeService = undefined
  (newable, options)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the takeback service retireves the list of takebacks.
    Service.promise = future.promise

    #the list of takebacks
    Service.list = []


    #update the list of takebacks
    Service.updateList = (succ, error)->
      $log.debug "list"
      Takeback.query(options, (list)->
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
        Takeback.update(newItem, ->
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        Takeback.save newItem, (savedItem)->
          Service.list.push(savedItem)
          (callback or angular.noop)(savedItem)

    Service.remove = (item)->
      return if($window.confirm("정말 반품을 취소하고 재고로 보내시겠습니까?") == false)
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
      throw Error "Cannot retrieve takebacks"

    beforeService = Service
    Service
])