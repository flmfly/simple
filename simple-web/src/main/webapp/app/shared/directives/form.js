/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('shared.directives.form', [
        'shared.services.data',
        'shared.application'
    ])

    .directive('dynamicForm', ['DataService', 'Session', 'APP_CONFIG', 'LoadingService',
        function (DataService, Session, APP_CONFIG, LoadingService) {
            return {
                restrict: 'A',
                templateUrl: 'app/shared/directives/form.tpl.html',
                replace: false,
                scope: {
                    entity: '=',
                    domain: '=',
                    readonly: '='
                },
                link: function (scope, element, attrs) {
                    LoadingService.show();
                    scope.debug = APP_CONFIG.DEBUG;

                    scope.tabs = [];

                    var initFields = function () {
                        DataService.getFormDesc(scope.domain.domainName).then(function (response) {
                            scope.fields = response.plain();

                            var setDefault = typeof scope.entity !== 'undefined' && typeof scope.entity.id === 'undefined';

                            var i, f;
                            for (i = 0; i < scope.fields.length; i++) {
                                f = scope.fields[i];

                                (function (field) {
                                    if (field.type.view === 'reference'
                                        && field.type.refDomainName === scope.domain.domainName) {
                                        scope.$emit('parent.found', field.name);
                                    }
                                })(f);

                                if (f.type.view === 'tab' || f.type.view === 'ATTACHMENT') {
                                    scope.tabs.push(f.name);
                                }

                                if (typeof f.defaultVal !== 'undefined' && setDefault) {
                                    scope.entity[f.name] = f.defaultVal;
                                }
                                if (typeof f.type.depend !== 'undefined') {
                                    (function (field) {

                                        scope.$watch('entity.' + field.type.depend, function (newValue, oldValue) {
                                            if (typeof oldValue === 'undefined' && typeof newValue === 'undefined') {
                                                return;
                                            }

                                            if (typeof oldValue !== 'undefined'
                                                && typeof newValue !== 'undefined'
                                                && newValue.id == oldValue.id) {
                                                return;
                                            }

                                            delete scope.entity[field.name];
                                        }, true);
                                    }(f));
                                }

                                if (f.hasChangedListener) {
                                    (function (field) {
                                        var watchKey;
                                        if (field.type.refId === undefined) {
                                            watchKey = 'entity.' + field.name;
                                        } else {
                                            watchKey = 'entity.' + field.name + "." + field.type.refId;
                                        }
                                        var inst=null;
                                        scope.$watch(watchKey, function (newValue, oldValue) {
                                        	 if(inst!=null){
                                             	clearTimeout(inst);
                                             }
                                        	 if ((newValue+"") == (oldValue+"")) {
                                                 return;
                                             }
                                           
                                            inst=setTimeout(function(){
                                            	var postData = {};

                                                for (var p in scope.entity) {
                                                    if (scope.entity.hasOwnProperty(p) && !_.isArray(scope.entity[p])) {
                                                        postData[p] = scope.entity[p];
                                                    }
                                                }

                                                DataService.changed(scope.domain.domainName, field.name, postData)
                                                    .then(function (response) {
                                                        var rtn = response.plain();
                                                        if (rtn.status.code === 200) {
                                                        	
                                                        	 for(var j =0 ;j< scope.fields.length;j++){
                                                                 var p=scope.fields[j].name;
                                                                 if (rtn.data.hasOwnProperty(p) && p !== field.name) {
                                                                     scope.entity[p] = rtn.data[p];
                                                                 }else if(scope.fields[j].type.view != 'tab' && p !== field.name){
                                                                     delete scope.entity[p];
                                                                 }
                                                        	 }

//                                                            for(var p in rtn.data){
//                                                                // copy all fields except the changed field, if do not remove the changed field
//                                                                // it will trigger the watch again, cause dead loop
//                                                                if (rtn.data.hasOwnProperty(p) && p !== field.name) {
//                                                                    scope.entity[p] = rtn.data[p];
//                                                                }
//                                                            }
                                                        }
                                                    });
                                            	
                                            },1000);
                                            
                                        });
                                    }(f));
                                }

                                if (typeof f.refField !== 'undefined') {
                                    (function (field) {
                                        var index = _.findIndex(scope.fields, function (chr) {
                                            return chr.name === field.refField;
                                        });

                                        if (index > -1) {
                                            scope.fields[index].fetch = true;
                                        }
                                        scope.$watch('entity.' + field.refField, function (newValue, oldValue) {
                                            if (typeof newValue !== 'undefined' && typeof oldValue !== 'undefined' && !_.isEqual(newValue, oldValue)) {

                                                var splited = field.refPath.split('.');
                                                var i = 1;
                                                var obj = null;
                                                if (newValue[splited[0]]) {
                                                    obj = newValue[splited[0]];
                                                }

                                                if (obj) {
                                                    for (; i < splited.length; i++) {
                                                        if (obj[splited[i]]) {
                                                            obj = obj[splited[i]];
                                                        } else {
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (obj) {
                                                    scope.entity[field.name] = obj;
                                                }
                                            } else if (typeof newValue === 'undefined' && typeof oldValue !== 'undefined') {
                                                delete scope.entity[field.name];
                                            }
                                        }, true);
                                    }(f));
                                }
                            }
                            if (scope.tabs.length === 0) {
                                setTimeout(function () {
                                    scope.$emit('domain.init.finished')
                                }, 0);
                            } else {
                                scope.$on('sub.init.finished', function (e, d) {
                                    scope.tabs.splice(_.indexOf(scope.tabs, d), 1);
                                    if (scope.tabs.length === 0) {
                                        setTimeout(function () {
                                            scope.$emit('domain.init.finished');
                                            scope.$broadcast('tab.clicked');
                                        }, 0);
                                    }
                                });
                            }

                            var tempGroups = _.groupBy(scope.fields, function (n) {
                                if (n.type.view === 'tab') {
                                    scope.hasTabs = true;
                                    return 'tabs';
                                }
                                return n.group.title;
                            });

                            scope.groups = [];

                            var prop;


                            for (prop in tempGroups) {
                                if (tempGroups.hasOwnProperty(prop) && prop !== 'tabs') {
                                    scope.groups.push({title: prop, fields: tempGroups[prop]});
                                }
                            }
                        });
                    };

                    var isNew = Session.domainId === null;

                    if (isNew) {
                        initFields();
                    } else {
                        scope.$on('domain.loaded', function (e, d) {
                            initFields();
                        });
                    }

                    //var mainTab = $('#main');
                    //
                    //scope.$watch(function () {
                    //    return mainTab;
                    //}, function () {
                    //    $(window).trigger('resize.chosen');
                    //}, true);


                    scope.tabClick = function (tabName) {
                        scope.$broadcast('tab.clicked', tabName);
                    };

                    scope.$on('$destroy', function () {

                    });

                }
            };
        }])

;