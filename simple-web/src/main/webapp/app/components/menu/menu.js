/**
 * Created by Jeffrey on 12/4/14.
 */
'use strict';
angular.module('components.menu', [])

    .directive('menu', ['$route', 'DataService', 'LoadingService', function ($route, DataService, LoadingService) {

        return {
            restrict: 'A',
            templateUrl: 'app/components/menu/menu.tpl.html',
            replace: false,
            scope: {
                //entity: '=',
                //field: '=',
                //type: '@'
            },
            controller: function ($scope, $location) {
                $scope.path = $location.path();
                $scope.menus = DataService.getMenu().$object;
            },
            link: function (scope, element, attrs) {
                scope.menuClick = function (path, canBeClick) {
                    if (canBeClick === true){
                        if(scope.path === path) {
                            $route.reload();
                        }
                        //else {
                        LoadingService.show();
                        //}
                        scope.path = path;
                    }


                    //if (path !== '' && path !== '/') {
                    //    LoadingService.show();
                    //}
                }
            }
        };

    }])

;