/**
 * Created by Jeffrey on 12/7/14.
 *  //rootUrl: 'http://192.168.11.2:8080/s/',
 //rootUrl: window.document.location.href.replace('/index.html','/').replace('/login.html','/')
 //window.document.location.pathname.replace('/index.html','/').replace('/login.html','/') + 'rest/api/',
 * //var index = window.document.location.href.indexOf('/index.html');
 //if(index === -1){
            //    index = window.document.location.href.indexOf('/login.html');
            //}
 //if( index !== -1){
            //    window.document.location.href.substring(0, index + 1);
            //}else{
            //    return window.document.location.href;
            //}
 */
'use strict';

angular.module('shared.services.data', ['ngResource', 'restangular'])
    .constant('URL_CONFIG', {
        getRootUrl: function () {
            //return 'http://192.168.11.2:8080/s/';
            return window.document.location.pathname.replace('/index.html', '/').replace('/login.html', '/');
        },
        getOrigin: function () {
            //return 'http://192.168.11.2:8080';
            return window.document.location.origin;
        },
        getRestBaseUrl: function () {
            return this.getRootUrl() + 'rest/api/';
        },
        getFuncBaseUrl: function () {
            return this.getRootUrl() + 'func/api/';
        }
    })
    .config(['URL_CONFIG', 'RestangularProvider',
        function (URL_CONFIG, RestangularProvider) {
            RestangularProvider.setBaseUrl(URL_CONFIG.getRestBaseUrl());
        }])
    .run(['Restangular', 'DialogService', 'LoadingService', '$rootScope',
        function (Restangular, DialogService, LoadingService, $rootScope) {
            Restangular
                .setFullRequestInterceptor(function (element, operation, what, url, headers, params) {
                    LoadingService.show();
                    return {
                        headers: headers,
                        params: _.extend(params, {
                            _t: new Date().getTime()
                        }),
                        element: element
                    };
                })
                .setResponseExtractor(function (resData, operation, route, fetchUrl, response, deferred) {
                    $rootScope.$broadcast('request.trigger');
                    if (typeof resData.status !== 'undefined'
                        && typeof resData.status.code !== 'undefined') {
                        if (resData.status.code === 401
                            && fetchUrl.indexOf('/base/login') === -1) {
                            window.location.href = 'login.html';
                        } else if (resData.status.code === 404) {
                            DialogService.alert(['您没有访问该功能的权限！']);
                        }
                    }
                    LoadingService.hide();
                    return resData;
                })
                .setErrorInterceptor(function (response, deferred, responseHandler) {
                    if (response.status !== 200) {
                        DialogService.dialog({
                            title: response.status + ' ' + response.statusText + '<br><h5>' + response.config.url + '</h5>',
                            message: response.data,
                            closeButton: true,
                            buttons: {
                                ok: {
                                    label: '<i class="ace-icon fa fa-check"></i> OK',
                                    className: 'btn-xs btn-success'
                                }
                            }
                        });
                        //LoadingServiceProvider.hide();
                        return false; // error handled
                    }

                    return true; // error not handled
                });
        }])
    .factory('DataService', ['Restangular', 'URL_CONFIG', '$cacheFactory',
        function (Restangular, URL_CONFIG, $cacheFactory) {

            __GLOBE.BASE_PATH = URL_CONFIG.getRootUrl();
            __GLOBE.ORIGIN = URL_CONFIG.getOrigin();

            // init caches
            $cacheFactory('domainDesc');
            $cacheFactory('formDesc');
            $cacheFactory('searchDesc');
            $cacheFactory('tableDesc');

            return {
                // deprecated replaced by getRestBaseUrl
                getBaseUrl: function () {
                    return URL_CONFIG.getRestBaseUrl();
                },
                getRestBaseUrl: function () {
                    return URL_CONFIG.getRestBaseUrl();
                },
                getFuncBaseUrl: function () {
                    return URL_CONFIG.getFuncBaseUrl();
                },
                getRootUrl: function () {
                    return URL_CONFIG.getRootUrl();
                },
                getTemplateUrl: function (domainName) {
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/template';
                },
                getImportUrl: function (domainName) {
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/import';
                },
                getImportFileUrl: function (domainName, fileNmae) {
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/import/file/' + fileNmae;
                },
                getExportUrl: function (domainName) {
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/export';
                },
                getImageUrl: function (domainName, path, fileName,category) {
                	if(!category){
                		category="";
                	}
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/file?path=' + path + '&fileName=' + (fileName ? fileName : '')+'&category='+category;
                },
                getVerifyCodeUrl: function () {
                    return URL_CONFIG.getRootUrl() + 'imageServlet';
                },
                getUploadUrl: function (domainName,category) {
                	if(!category){
                		category="";
                	}
                    return URL_CONFIG.getFuncBaseUrl() + domainName + '/upload?category='+category;
                },


                // core rest api
                get: function (domainName, id) {
                    return Restangular.service(domainName).one(id).get();
                },
                getList: function (domainName) {
                    return Restangular.service(domainName).getList();
                },
                getSubList: function (domainName, id, refName) {
                    return Restangular.service(domainName).one(id).all(refName).getList();
                },
                getTree: function (domainName) {
                    return Restangular.service(domainName).one('tree').getList();
                },
                getRefTree: function (domainName, refName) {
                    return Restangular.service(domainName).one('ref').all(refName).getList();
                },
                getDependTree: function (refDomainName, dependAssociateField, dependId) {
                    return Restangular.service(refDomainName).one('tree').one(dependAssociateField).all(dependId).getList();
                },


                getDomainDesc: function (domainName) {
                    var cacheEngine = $cacheFactory.get('domainDesc');
                    if (!cacheEngine) {
                        cacheEngine = $cacheFactory('domainDesc');
                    }

                    var cache = cacheEngine.get(domainName);
                    if (!cache) {
                        cache = Restangular.service(domainName).one('desc').one('domain').get();
                        cacheEngine.put(domainName, cache);
                    }
                    return cache;
                },
                getFormDesc: function (domainName) {
                    //var cacheEngine = $cacheFactory.get('formDesc');
                    //var cache = cacheEngine.get(domainName);
                    //if (!cache) {
                    //    cache = Restangular.service(domainName).one('desc').all('form').getList();
                    //    cacheEngine.put(domainName, cache);
                    //}
                    //
                    //return cache;
                    return Restangular.service(domainName).one('desc').all('form').getList();
                },
                getSearchDesc: function (domainName) {
                    var cacheEngine = $cacheFactory.get('searchDesc');
                    var cache = cacheEngine.get(domainName);
                    if (!cache) {
                        cache = Restangular.service(domainName).one('desc').all('search').getList();
                        cacheEngine.put(domainName, cache);
                    }

                    return cache;
                },
                getTableDesc: function (domainName) {
                    var cacheEngine = $cacheFactory.get('tableDesc');
                    var cache = cacheEngine.get(domainName);
                    if (!cache) {
                        cache = Restangular.service(domainName).one('desc').all('table').getList();
                        cacheEngine.put(domainName, cache);
                    }

                    return cache;
                },


                getPage: function (domainName, example, page) {
                    if (typeof page !== 'undefined'
                        && typeof page.sort === 'undefined') {
                        page.sort = "";
                    }
                    return Restangular.service(domainName).one('page').post('', example, page, {});
                },
                save: function (domainName, domain) {
                    return Restangular.service(domainName).post(domain);
                },
                update: function (domainName, id, domain) {
                    return Restangular.service(domainName).one(id).customPUT(domain);
                },
                remove: function (domainName, id) {
                    return Restangular.service(domainName).one(id).remove();
                },
                removeAll: function (domainName, rowids) {
                    return Restangular.service(domainName).one('delete').customPOST(rowids);
                },
                validate: function (domainName, domain) {
                    return Restangular.service(domainName).one('validate').post('', domain, {}, {});
                },
                operate: function (domainName, operationCode, data) {
                    return Restangular.service(domainName).one('operation').one(operationCode).customPOST(data);
                },
                changed: function (domainName, fieldName, data) {
                    return Restangular.service(domainName).one('changed').one(fieldName).customPOST(data);
                },
                spec: function (domainName, data) {
                    return Restangular.service(domainName).one('spec').customPOST(data);
                },

                // base api
                getBaseService: function () {
                    return Restangular.service('base');
                },
                getMenu: function () {
                    return this.getBaseService().one('menu').getList();
                },
                login: function (user) {
                    return this.getBaseService().one('login').post('', user, {}, {});
                },
                logout: function () {
                    return this.getBaseService().one('logout').get();
                },
                getLoginUser: function () {
                    return this.getBaseService().one('loginuser').get();
                },
                changePassword: function (user) {
                    return this.getBaseService().one('changepassword').post('', user, {}, {});
                },
                getMessage: function (maxId) {
                    return this.getBaseService().one('message').customPOST(maxId);
                },
                setMessageRead: function (ids) {
                    return this.getBaseService().one('message').one('setread').customPOST(ids);
                }
            };
        }]);