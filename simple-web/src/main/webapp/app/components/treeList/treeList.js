/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('components.treeList', [])

    .controller('TreeListCtrl', ['$scope', 'APP_CONFIG', 'Session', 'DataService',
        function ($scope, APP_CONFIG, Session, DataService) {
            $scope.debug = APP_CONFIG.DEBUG;

            $scope.$on('domain.saved', function (event, data) {
                $scope.$broadcast('tree.refresh', {
                    url: DataService.getBaseUrl() + Session.domainName + "/tree",
                    node: data,
                    single: true
                });
            });
            $scope.$on('domain.deleted', function (event, data) {
                $scope.$broadcast('tree.refresh', {
                    url: DataService.getBaseUrl() + Session.domainName + "/tree",
                    node: data,
                    single: true
                });
            });

            $scope.$on('node.click', function (event, data) {
                DataService.get(Session.domainName, data).then(function (response) {
                    var rtnData = response.plain();
                    if (rtnData.status.code === 200) {
                        $scope.$broadcast('domain.changed', rtnData.data);
                    }
                });
            });

            $scope.$on('tree.directive.loaded', function (event, data) {
                $scope.$broadcast('tree.refresh', {
                    url: DataService.getBaseUrl() + Session.domainName + "/tree",
                    node: {},
                    single: true
                });
            });
        }])
;