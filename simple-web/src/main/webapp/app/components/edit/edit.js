/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('components.edit', [])

    .controller('EditCtrl', [
        '$scope',
        'DataService',
        'Session',
        'DialogService',
        'LoadingService',
        '$location',
        '$route',
        function ($scope, DataService, Session, DialogService, LoadingService, $location, $route) {
            $scope.saveSuccess = false;
            $scope.isNew = Session.domainId === null;
            $scope.showAddMore = false;

            if ($scope.isNew) {
                $scope.entity = {};
            } else {
                DataService
                    .get(Session.domainName, Session.domainId)
                    .then(function (response) {
                        var rtnData = response.plain();
                        if (rtnData.status.code === 200) {
                            $scope.entity = rtnData.data;
                            $scope.$broadcast('domain.loaded');
                        }
                    });
            }

            $scope.domain = {
                domainName: Session.domainName
            };

            $scope.$on('domain.changed', function (event, data) {
                $scope.entity = data;
            });

            $scope.$watch('category', function (newValue, oldValue) {
                if (typeof newValue !== 'undefined') {
                    DataService.get(Session.parent.name, newValue.id).then(function (response) {
                        $scope.category = response.plain().data;
                        $scope.entity[Session.parent.prop] = $scope.category;
                        $scope.$emit('category.changed', newValue);
                    });
                }
            }, true);

            $scope.add = function () {
                $scope.entity = {};

                if ($scope.category) {
                    $scope.entity[Session.parent.prop] = $scope.category;
                }
            };

            $scope.addSub = function () {
                var tmp = $scope.entity;
                $scope.entity = {};
                if ($scope.category) {
                    $scope.entity[Session.parent.prop] = $scope.category;
                }
                $scope.entity.parent = tmp;
            };

            $scope.del = function () {
                DataService.remove(Session.domainName, $scope.entity.id).then(function (response) {
                    var rtnData = response.plain();

                    if (rtnData.status.code === 200) {
                        DialogService.alert(['删除成功！']);
                        $scope.add();
                        $scope.$emit('domain.deleted', $scope.entity);
                    } else {
                        DialogService.alert(rtnData.infos);
                    }
                });
            };

            $scope.addNext = function () {
                delete $scope.error;
                $scope.saveSuccess = false;
                $scope.entity = {};
                $scope.showAddMore = false;
                Session.setDomainId(null);
                $route.reload();
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
                            $scope.entity = rtnData.data;
                            Session.domainId = $scope.entity.id;
                            delete $scope.error;
                            $scope.$emit('domain.saved', $scope.entity);
                            $scope.$broadcast('domain.saved', $scope.entity);
                            $scope.protoEntity = _.cloneDeep($scope.entity, function (value) {
                                return _.isBoolean(value) ? '' + value : value;
                            });

                            $scope.saveSuccess = true;
                            $scope.showAddMore = true;
                            DataService.getDomainDesc(Session.domainName).then(function (response) {
                                var domainDesc = response.plain();

                                if (typeof domainDesc.standarOperation !== 'undefined' && domainDesc.standarOperation.modify === false) {
                                    Session.setDomainId(null);
                                    $location.url('/list/' + Session.domainName);
                                }
                            });
                        } else {
                            infos = rtnData.infos;
                            if (!infos) {
                                infos = rtnData.status.messages;
                            }
                            if (!infos) {
                                infos = ["系统错误！"];
                            }
                            if (typeof(infos) === "string") {
                                infos = [infos];
                            }
                            $scope.error = infos.join('\n');
                            $scope.saveSuccess = false;
                            $(window).scrollTop(0);
                        }

                        //DialogService.alert(infos, callback);
                    }
                )
                ;
            };

            $scope.removeError = function () {
                delete $scope.error;
            };

            $scope.$on('domain.init.finished', function () {
                $scope.protoEntity = _.cloneDeep($scope.entity);
                LoadingService.hide();
                $(window).trigger('resize.chosen');
            });

            $scope.back = function () {
                if (!_.isEqual($scope.entity, $scope.protoEntity)) {
                    DialogService.confirm(['您已经对该页面的数据进行了更改，您确定要放弃更改吗？'], function () {
                        delete $scope.protoEntity;
                        Session.setDomainId(null);
                        history.back(-1);
                    });
                } else {
                    delete $scope.protoEntity;
                    Session.setDomainId(null);
                    history.back(-1);
                }
            };

            $scope.$on('parent.found', function(event, data){
                $scope.$watch('entity[\'' + data + '\']', function (newValue) {
                    if(newValue && newValue.id == $scope.entity.id){
                        DialogService.alert(['自己不能作为自己的上级!'], function(){
                            $scope.$apply(function(){
                                delete $scope.entity[data];
                            });
                        });
                    }
                }, true);
            });
        }
    ])
;