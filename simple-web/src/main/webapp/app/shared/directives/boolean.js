/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('shared.directives.boolean', [])

    .directive('boolean', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            template: '<span ng-repeat="item in field.type.options" style="line-height: 30px"><input type="radio" class="ace input-sm" ng-model="entity[field.name]" ng-value="item.id"><span class="lbl">&nbsp;{{item.name}}&nbsp;&nbsp;</span></span>',
            replace: true,
            scope: {
                entity: '=',
                field: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('entity.' + scope.field.name, function(newValue){
                    if(typeof scope.entity === 'undefined'){
                        return;
                    }
                    if(typeof newValue !== 'undefined')
                        scope.entity[scope.field.name] = newValue + '';
                }, true);
            }
        }
    }])
;