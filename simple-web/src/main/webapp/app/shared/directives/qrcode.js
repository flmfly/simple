/**
 * Created by liuweichun on 16/05/20.
 */

angular.module('shared.directives.qrcode', [])

    .directive('qrcode', ['DataService', 'Session', 'DialogService', function (DataService, Session, DialogService) {
        return {
            restrict: 'A',
            template: '<div ng-show="entity[field.name]"></div>',
            replace: true,
            scope: {
                field: '=',
                entity: '=',
                width : '=',
                height : '='
            },
            link: function (scope, element, attrs) {
            	var qrcode = new QRCode(element[0], {
    				width : scope.width? scope.width:100,
    				height : scope.height? scope.height:100
    			});
    			  if(scope.entity[scope.field.name]){
                    	 qrcode.makeCode(scope.entity[scope.field.name]);
                     }
            	 scope.$watch('entity.' + scope.field.name, function(newValue){
                     if(typeof scope.entity === 'undefined'){
                         return;
                     }
                     if(typeof newValue !== 'undefined'){
                         scope.entity[scope.field.name] = newValue + '';
                         qrcode.makeCode(scope.entity[scope.field.name]);
                     }
                 }, true);
            }
        }
    }])
;