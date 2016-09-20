/**
 * Created by Jeffrey on 12/7/14.
 */

angular.module('app', [
    'shared.application',
    'shared.session',

    'shared.services.dialog',
    'shared.services.data',
    'shared.services.loading',
    'shared.services.gallery',
    'shared.services.message',
    'shared.services.utils',

    'components.menu',
    'components.list',
    'components.edit',
    'components.treeList',
    'components.switchTreeList',
    'components.subList',
    'components.selectedSubList',
    'components.homepage',
    'components.map',
    'components.special',

    'shared.directives.tree',
    'shared.directives.referenceTree',
    'shared.directives.referenceTable',
    'shared.directives.referenceMap',
    'shared.directives.referenceView',
    'shared.directives.form',
    'shared.directives.search',
    'shared.directives.datetimepicker',
    'shared.directives.jqGrid',
    'shared.directives.chosen',
    'shared.directives.boolean',
    'shared.directives.uploader',
    'shared.directives.tagsInput',
    'shared.directives.spend',
    'shared.directives.formFields',
    'shared.directives.messageView',
    'shared.directives.attachment',
    'shared.directives.htmlEditor',
    'shared.directives.qrcode',
    'ngRoute',
    'pasvaz.bindonce'
])
    //.constant('APP_CONFIG', {
    //    DEBUG:true
    //})
    .controller('LogoutController', ['$scope', 'DataService', function ($scope, DataService, DialogService) {
        $scope.logout = function () {
            DataService.logout();
            window.location.href = 'login.html';
        };

    }])

    .controller('NIYCtrl', ['$scope', 'DialogService', function ($scope, DialogService) {
        $scope.NIY = function () {
            DialogService.alert(['敬请期待！']);
        };
    }])

    .run(function ($rootScope) {
        //$rootScope.debug = true;
    })

    .config(function ($routeProvider, $provide) {
        /**
         * overwrite angular's directive ngSwitchWhen
         * can handle ng-switch-when="value1 || value2 || value3"
         */
        $provide.decorator('ngSwitchWhenDirective', function ($delegate) {
            $delegate[0].compile = function (element, attrs, transclude) {
                return function (scope, element, attr, ctrl) {
                    var subCases = [attrs.ngSwitchWhen];
                    if (attrs.ngSwitchWhen && attrs.ngSwitchWhen.length > 0 && attrs.ngSwitchWhen.indexOf('||') != -1) {
                        subCases = attrs.ngSwitchWhen.split('||');
                    }
                    var i = 0;
                    var casee;
                    var len = subCases.length;
                    while (i < len) {
                        casee = $.trim(subCases[i++]);
                        ctrl.cases['!' + casee] = (ctrl.cases['!' + casee] || []);
                        ctrl.cases['!' + casee].push({transclude: transclude, element: element});
                    }
                }
            }
            return $delegate;
        });
    });
;