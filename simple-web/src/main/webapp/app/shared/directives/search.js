/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('shared.directives.search', [])

    .directive('dynamicSearch', ['DataService', 'APP_CONFIG', function (DataService, APP_CONFIG) {
        return {
            restrict: 'A',
            templateUrl: 'app/shared/directives/search.tpl.html',
            replace: false,
            //controller:function($scope){
            //    console.log($scope.domain.domainName);
            //    $
            //},
            scope: {
                entity: '=',
                domain: '='
            },
            link: function (scope, element, attrs) {
                scope.debug = APP_CONFIG.DEBUG;

                scope.identifier = new Date().getTime();

                DataService.getSearchDesc(scope.domain.domainName).then(function (response) {
                    scope.fields = response.plain();
                    var i;
                    for (i = 0; i < scope.fields.length; i++) {
                        var f = scope.fields[i];
                        if (typeof f.type.depend !== 'undefined') {
                            (function(f){
                                var fieldName = f.name;
                                scope.$watch('entity.' + f.type.depend, function (newValue, oldValue) {
                                    if (typeof oldValue === 'undefined') {
                                        return;
                                    }
                                    delete scope.entity[fieldName];
                                }, true);
                            })(f);
                        }
                    }
                });

                scope.toggleFuzzy = function (field, e) {
                    var target = $(e.target).closest('span');
                    var fuzzy = false;
                    if (typeof scope.entity[field.name] === 'undefined') {
                        scope.entity[field.name] = {};
                    }
                    if (typeof scope.entity[field.name]['fuzzy'] === 'undefined'
                        || !scope.entity[field.name]['fuzzy']) {
                        fuzzy = true;
                    }

                    scope.entity[field.name]['fuzzy'] = fuzzy;
                };

                scope.inputChange = function (field, e) {
                    if (typeof field.validation !== 'undefined'
                        && typeof field.validation.pattern !== 'undefined') {

                        var val = e.target.value;
                        if (field.validation.type === 'double') {
                            var scale = field.validation.pattern.substring(field.validation.pattern.indexOf('.') + 1).length;

                            if (val.indexOf('.') !== -1) {
                                var splitted = val.split('.');
                                if (splitted[1].length > scale) {
                                    e.target.value = val.substring(0, val.length - 1);
                                }
                            } else if (val.length > field.validation.pattern.indexOf('.')) {
                                e.target.value = val.substring(0, val.length - 1);
                            }
                        } else if (field.validation.type === 'long') {
                            if (val.length > field.validation.pattern.length) {
                                e.target.value = val.substring(0, val.length - 1);
                            }
                        }
                    }
                };

                scope.elm = {};

                scope.inputClick = function (field, e) {
                    if (typeof field.validation !== 'undefined'
                        && typeof field.validation.length !== 'undefined'
                        && typeof scope.elm[e.target.id] === 'undefined') {
                        var elm = $(e.target);
                        elm.inputlimiter({
                            useMaxlength: false,
                            limit: field.validation.length,
                            remText: '还剩%n个字符可以输入',
                            limitText: '最多只能输入%n个字符'
                        });

                        if (field.validation.type === 'long') {
                            elm.keydown(function (e) {
                                // Allow: backspace, delete, tab, escape, enter and .
                                if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110]) !== -1 ||
                                        // Allow: Ctrl+A
                                    (e.keyCode == 65 && e.ctrlKey === true) ||
                                        // Allow: home, end, left, right
                                    (e.keyCode >= 35 && e.keyCode <= 39)) {
                                    // let it happen, don't do anything
                                    return;
                                }
                                // Ensure that it is a number and stop the keypress
                                if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
                                    e.preventDefault();
                                }
                            });
                        } else if (field.validation.type === 'double') {
                            elm.keydown(function (e) {

                                if (e.keyCode === 190) {
                                    if (elm.val().indexOf('.') !== -1) {
                                        e.preventDefault();
                                        return;
                                    }
                                }

                                // Allow: backspace, delete, tab, escape, enter and .
                                if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110, 190]) !== -1 ||
                                        // Allow: Ctrl+A
                                    (e.keyCode == 65 && e.ctrlKey === true) ||
                                        // Allow: home, end, left, right
                                    (e.keyCode >= 35 && e.keyCode <= 39)) {
                                    // let it happen, don't do anything
                                    return;
                                }
                                // Ensure that it is a number and stop the keypress
                                if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
                                    e.preventDefault();
                                }

                                if (elm.val().length >= field.validation.length) {
                                    e.preventDefault();
                                }
                            });
                        } else if (field.validation.type === 'string') {
                            elm.keydown(function (e) {
                                if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110]) === -1) {
                                    if (elm.val().length >= field.validation.length) {
                                        e.preventDefault();
                                    }
                                }
                            });
                        }
                        scope.elm[e.target.id] = elm;
                    }
                };
            }
        };
    }])

;