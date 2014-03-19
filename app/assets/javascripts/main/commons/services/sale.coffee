angular.module('services.sale',[
  'ngResource'
  'constants'
]).factory('Sale',['$resource', 'PATH', ($resource, PATH)->
  $resource(PATH.api + "/sales/:id",{id:'@id'},
    update:
      method:'PUT'
    getItem :
      method:'GET'
      isArray:false
  )
]).factory('SaleService', ['Sale', '$q', '$window', '$log', 'PATH', '$http','$filter', (Sale, $q, $window, $log, PATH, $http, $filter)->
  beforeService = undefined
  (newable, options)->
    return beforeService if(!newable and beforeService)

    future = $q.defer()

    Service = @

    #It will be resolved, when the sale service retireves the list of sales.
    Service.promise = future.promise

    #the list of sales
    Service.list = []


    #update the list of sales
    Service.updateList = (succ, error)->
      $log.debug "list"
      Sale.query(options, (list)->
        Service.list = list
        (succ or angular.noop)(list)
      , ->
        (error or angular.noop)()
      )

    #save a new item
    Service.save = (newItem, callback)->
      newItem.buyerName= newItem.buyerName.replace(/\s/g,"")
      return $window.alert("이름을 정확히 입력해 주세요.") if(angular.isArray(newItem.buyerName.match(/\*/)) or newItem.buyerName.length == 0)

      if(newItem.id)
        oldItem = Service.get(newItem)
        index = Service.list.indexOf(oldItem)
        $log.debug oldItem, index
        Sale.update(newItem, ->
          $log.debug newItem
          Service.list.splice(index,1,newItem)
          (callback or angular.noop)(newItem)
        )
      else
        phone = newItem.phone
        if(phone.length > 3)
          newItem.phoneNumberTail = phone.slice(phone.length-4, phone.length)
          newItem.phoneNumberHead = phone.slice(0, phone.length-4)

        Sale.save newItem, (savedItem)->
          Service.list.push(savedItem)
          (callback or angular.noop)(savedItem)

    Service.remove = (item)->
      return if($window.confirm("정말 판매를 취소하시겠습니까?") == false)
      item.$remove(->
        index = Service.list.indexOf(item)
        Service.list.splice(index, 1)
      )

    Service.detail =(item, callback) -> #판매 detail을 받는다.
      oldItem = angular.copy(item)
      item.$getItem().then (newItem)->
#        for item of oldItem
#          newItem[item] = oldItem[item] if(!newItem[item])
        angular.extend(newItem, oldItem)
        $log.debug(newItem)
        newItem.saleDate = $filter('date')( newItem.createDate, 'yyyy-MM-dd')
        (callback or angular.noop)(newItem)
        newItem


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

    Service.takeBack = (saleId, reason, success, failure)->
      $http.post("#{PATH.api}/sales/#{saleId}/takeback",
        reason:reason
      ).success (data, status, headers, config)->
        Service.findById(saleId).status = 3
        (success or angular.noop)()
      .error (data, status, headers, config)->
          (failure or angular.noop)(status)



    #Service initialization
    Service.updateList ->
      future.resolve Service
    , ->
      future.reject()
      throw Error "Cannot retrieve sales"

    beforeService = Service
    Service
])