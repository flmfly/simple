/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('components.switchTreeEdit', [])

    .controller('SwitchTreeEditCtrl', [
        '$scope',
        'DataService',
        'Session',
        'DialogService',
        function ($scope, DataService, Session, DialogService) {
            var isNew = Session.domainId === null;

            if (isNew) {
                $scope.entity = {};
            } else {
                DataService
                    .get(Session.domainName, Session.domainId)
                    .then(function (response) {
                        var rtnData = response.plain();
                        if (rtnData.status.code === 200) {
                            $scope.entity = rtnData.data;
                        }
                    });
            }

            $scope.domain = {
                domainName: Session.domainName
            };

            $scope.$on('domain.changed', function (event, data) {
                $scope.entity = data;
            });

            $scope.add = function () {
                $scope.entity = {};
            };

            $scope.addSub = function () {
                var tmp = $scope.entity;
                $scope.entity = {};
                $scope.entity.parent = tmp;
            };

            $scope.del = function () {
                DataService.remove(Session.domainName, $scope.entity.id).then(function (response) {
                    var rtnData = response.plain();

                    if (rtnData.status.code === 200) {
                        DialogService.alert(['删除成功！']);
                        $scope.$emit('domain.deleted', $scope.entity);
                    } else {
                        DialogService.alert(rtnData.infos);
                    }
                });
            };

            $scope.save = function () {
                var post = null;
                if (typeof $scope.entity.id === 'undefined') {
                    post = DataService.save(Session.domainName, $scope.entity);
                } else {
                    post = DataService.update(Session.domainName, $scope.entity.id, $scope.entity);
                }
                post.then(function (response) {
                    var rtnData = response.plain(),
                        valid = rtnData.status.code === 200,
                        callback = null,
                        infos = null;

                    if (valid) {
                        //callback = function () {
                        //    history.back(-1);
                        //};
                        infos = ['保存成功！'];
                        $scope.entity = rtnData.data;
                    } else {
                        infos = rtnData.infos;
                    }

                    DialogService.alert(infos, callback);

                    $scope.$emit('domain.saved', $scope.entity);
                    //$scope.$broadcast('domain.saved');
                });
            };

        }])
;