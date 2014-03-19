angular.module('services.model',[
  'ngResource'
  'constants'
  'services.modelcolor'
]).factory('Model',['$resource', '$route', '$log', 'PATH',($resource, $route, $log, PATH)->
  $resource(PATH.api + "/models/:id/:colors",{id:'@id'},
    update:
      method:'PUT'
  )
]).factory('ModelService', ['Model', '$q', '$window',(Model, $q, $window)->
  beforeService = undefined

  (newable)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the model service retireves the list of models.
    Service.promise = future.promise

    #the list of models
    Service.list = []


    #update the list of models
    Service.updateList = (succ, error)->
      Model.query (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()

    #save a new item
    Service.save = (newItem, callback)->
      newItem.name = newItem.name.replace(/-|_| /g,"").toUpperCase()
      if(newItem.colors.length is 0)
        $window.alert("색상이 1가지 이상은 존재해야 합니다.")
        return
      if(newItem.id)
        oldItem = Service.get(newItem)
        index = Service.list.indexOf(oldItem)
        Model.update(newItem, ->
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        newItem.colors = newItem.colors.map((item)->item.color)
        Model.save newItem, (savedItem)->
          Service.list.push(savedItem)
          (callback or angular.noop)(savedItem)

    Service.remove = (item)->
      return if($window.confirm("정말 삭제 하시겠습니까?") == false)
      item.$remove(->
        index = Service.list.indexOf(item)
        Service.list.splice(index, 1)
      , (error)->
#        if(error.status is 403)
#          $window.context("해당 자료를 숨길까요?")
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
      throw Error "Cannot retrieve models"

    beforeService = Service
    Service
])