/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('components.special', [])
    .controller('SpecialCtrl', [
        '$scope',
        '$rootScope',
        '$window',
        'DataService',
        '$location',
        '$route',
        'Session',
        'DialogService',
        'GalleryService',
        'DEFAULT_PAGE_SIZES',
        'DEFAULT_PAGE',
        'APP_CONFIG',
        '$compile',
        function ($scope, $rootScope, $window, DataService,
                  $location, $route, Session, DialogService,
                  GalleryService,
                  DEFAULT_PAGE_SIZES, DEFAULT_PAGE, APP_CONFIG,
                  $compile) {
            $scope.query = Session.query === null ? {} : Session.query;
            Session.query = null;

            if (Session.property !== null) {
                $scope.query[Session.property] = {id: Session.propId};
            }

            $scope.domain = {
                domainName: Session.domainName
            };

            $scope.queryAreaToggle = {
                hideButtonName: '展开',
                showButtonName: '收起',
                style: {overflow: 'hidden', height: '0px'},
                isShown: false
            };

            $scope.toggle = function () {
                $scope.queryAreaToggle.isShown = !$scope.queryAreaToggle.isShown;
                $scope.applyToggle();
            };

            $scope.applyToggle = function () {
                if (!$scope.queryAreaToggle.isShown) {
                    $scope.queryAreaToggle.style = {overflow: 'hidden', height: '0px'};
                } else {
                    $scope.queryAreaToggle.style = {};
                }
            };

            $scope.queryData = function(){
                DataService.spec(Session.domainName, $scope.query)
                    .then(function (response) {
                        var rtn = response.plain();
                        if (rtn.status.code === 200) {
                            $scope.result = rtn.data;
                        }
                    });
            };

        }])

;