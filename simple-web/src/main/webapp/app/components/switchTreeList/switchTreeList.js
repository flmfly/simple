/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('components.switchTreeList', [])

    .controller('SwitchTreeListCtrl', ['$scope', 'APP_CONFIG', 'Session', 'DataService', function ($scope, APP_CONFIG, Session, DataService) {
        $scope.debug = APP_CONFIG.DEBUG;

        $scope.categorys = DataService.getTree(Session.parent.name).$object;

        $scope.$on('category.changed', function (event, newValue) {
            if (typeof newValue !== 'undefined') {
                Session.parent.id = newValue.id;
                $scope.$broadcast('tree.refresh', {
                    url: DataService.getBaseUrl() + Session.domainName + "/tree/" + Session.parent.prop + "/" + newValue.id,
                    node: {},
                    single: true
                });
            }
        });

        //$scope.$watch('category', function (newValue, oldValue) {
        //
        //}, false);

        $scope.$on('node.click', function (event, data) {
            DataService.get(Session.domainName, data).then(function (response) {
                var rtnData = response.plain();
                if (rtnData.status.code === 200) {
                    $scope.$broadcast('domain.changed', rtnData.data);
                }
            });
        });

        $scope.$on('domain.saved', function (event, data) {
            $scope.$broadcast('tree.refresh', {
                url: DataService.getBaseUrl() + Session.domainName + "/tree/" + Session.parent.prop + "/" + data[Session.parent.prop].id,
                node: data,
                single: true
            });
        });
        $scope.$on('domain.deleted', function (event, data) {
            $scope.$broadcast('tree.refresh', {
                url: DataService.getBaseUrl() + Session.domainName + "/tree/" + Session.parent.prop + "/" + Session.parent.id,
                node: data,
                single: true
            });
        });
    }])
;