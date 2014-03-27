angular.module("constants", [
  'constants.path'
  'constants.status'
  'constants.telecom'
])

angular.module("constants.path",[])
  .constant("PATH",
    root: "/app"
    api : "/api"
    inventoryTemplate: "/assets/templates/inventory"
    template: "/assets/templates"
  )
angular.module("constants.status",[])
  .constant("STATUS",
    0 : '삭제됨'
    1 : '재고'
    2 : '판매됨'
    3 : '반품됨'
  )
angular.module("constants.telecom",[])
.constant("TELECOM",
    1 : 'SKT'
    2 : 'KT'
    3 : 'LGT'
  )

