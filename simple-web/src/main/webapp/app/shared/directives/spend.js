/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('shared.directives.spend', [])

    .directive('spend', [function () {
        return {
            restrict: 'A',
            template: '<i class="fa fa-clock-o orange" ng-class="spend == undefined?\'hidden\':\'\'">&nbsp;{{spend}}s </i>',
            replace: true,
            link: function (scope, element, attrs) {
                scope.spend;
                scope.startTime = new Date().getTime();
                scope.$on('spend.setStart', function(){
                    scope.startTime = new Date().getTime();
                });

                scope.$on('spend.finished', function(){
                    scope.spend = (new Date().getTime() - scope.startTime) / 1000;
                });
            }
        }
    }])
;