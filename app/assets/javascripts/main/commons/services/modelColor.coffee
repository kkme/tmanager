angular.module('services.modelcolor',[
  'ngResource'
  'constants'
]).factory('ModelColor',['$resource', 'PATH',($resource, PATH)->
  $resource(PATH.api + "/models/:modelId/colors/:id", {modelId:'@modelId', id:'@id'},
    update:
      method:'PUT'
  )
]).factory('ModelColorService', ['ModelColor', '$q', '$window', (ModelColor, $q, $window)->
  (model)->
    query =
      modelId : model.id


    future = $q.defer()

    Service = @

    #It will be resolved, when the modelcolor service retireves the list of model colors.
    Service.promise = future.promise

    #the list of modelcolors
    Service.list = []


    #update the list of model colors
    Service.updateList = (succ, error)->
      ModelColor.query query, (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()

    #save a new item
    Service.save = (newItem, callback)->
      return $window.alert "동일한 색상이 존재합니다." if(!newItem.id and (Service.list.filter (item)->
        item.color is newItem.color).length > 0)

      newItem.modelId = model.id
      if(!!newItem.modelId)
        if(!!newItem.id)
          oldItem = Service.get(newItem)
          index = Service.list.indexOf(oldItem)
          ModelColor.update(newItem, ->
            Service.list.splice(index,1,newItem)
            (callback or angular.noop)(newItem)
          )
        else
          ModelColor.save(newItem, (savedItem)->
            Service.list.push(savedItem)
            (callback or angular.noop)(newItem)
          )
      else
        Service.list.push(newItem)
        (callback or angular.noop)(newItem)

    Service.remove = (item)->
      return $window.alert("색상이 적어도 한가지는 존재해야 합니다.") if(Service.list.length < 2)
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


    if(!model.id)
      future.resolve('')
      return Service

    #Service initialization
    Service.updateList ->
      future.resolve Service
    , ->
      future.reject()
      throw Error "Cannot retrieve modelcolors"

    Service
]).factory('ColorManager',['ModelColorService',(ModelColorService)->
  list = {}
  (newable)->
    if(newable)
      list = {}
    Service = {}
    Service.getColors = (modelId)->
      if(!list[modelId])
        list[modelId] = new ModelColorService(id:modelId)
      list[modelId]

    Service
])