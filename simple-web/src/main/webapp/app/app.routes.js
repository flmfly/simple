/**
 * Created by Jeffrey on 12/7/14.
 */

angular.module('app')
    .config(['$routeProvider', '$locationProvider', 'SessionProvider',
        function ($routeProvider, $locationProvider, SessionProvider) {
            $locationProvider.html5Mode(false).hashPrefix('!');

            this.switchDomain = function (urlattr) {
                if (urlattr.name !== SessionProvider.domainName) {
                    SessionProvider.reset();
                }
                SessionProvider.setDomainName(urlattr.name);
                if (typeof urlattr.id !== 'undefined') {
                    SessionProvider.setDomainId(urlattr.id);
                }

                if (urlattr.parent) {
                    SessionProvider.setParent({
                        name: urlattr.parent,
                        prop: urlattr.parentProp
                    });
                }

                if (urlattr.property) {
                    SessionProvider.setProp(urlattr.property, urlattr.propId);
                } else {
                    SessionProvider.setProp(null, null);
                }

                // clear all bootbox modal dialog
                $('.bootbox').remove();
            };

            $routeProvider
                .when('/list/:name', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/list/list.tpl.html';
                    },
                    controller: 'ListCtrl'
                })
                .when('/map/:name', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/map/map.tpl.html';
                    },
                    controller: 'MapCtrl'
                })
                .when('/list/:name/:property/:propId', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/list/list.tpl.html';
                    },
                    controller: 'ListCtrl'
                })
                .when('/page/add/:name', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/edit/edit.tpl.html';
                    },
                    controller: 'EditCtrl'
                })
                .when('/page/edit/:name/:id', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/edit/edit.tpl.html';
                    },
                    controller: 'EditCtrl'
                })
                .when('/tree/:name', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/treeList/treeList.tpl.html';
                    },
                    controller: 'TreeListCtrl'
                })
                .when('/tree/:name/:parent/:parentProp', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/switchTreeList/switchTreeList.tpl.html';
                    },
                    controller: 'SwitchTreeListCtrl'
                })
                .when('/spec/:name', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/special/special.tpl.html';
                    },
                    controller: 'SpecialCtrl'
                })
                .when('/', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'app/components/homepage/homepage.tpl.html';
                    },
                    controller: 'HomepageCtrl'
                })
                .when('/charts/:type', {
                    templateUrl: function (urlattr) {
                        switchDomain(urlattr);
                        return 'charts/' + urlattr.type + '/' + urlattr.type + '.tpl.html';

                    }
                })
                .otherwise({redirectTo: '/'});

        }])
;