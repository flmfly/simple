/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('shared.directives.formFields', [])

    .directive('formFields', ['DataService', 'Session', 'APP_CONFIG', 'LoadingService',
        function (DataService, Session, APP_CONFIG, LoadingService) {
            return {
                restrict: 'A',
                templateUrl: 'app/shared/directives/formFields.tpl.html',
                replace: true,
                scope: {
                    entity: '=',
                    fields: '=',
                    domain: '=',
                    readonly: '='
                },
                link: function (scope, element, attrs) {
                    scope.normalFormGroupCss = 'col-lg-4 col-md-6 col-sm-6 col-xs-12';
                    scope.normalTitleCss = 'control-label input-sm col-lg-4 col-md-4 col-sm-4 field-title';
                    scope.normalFieldCss = 'col-lg-8 col-md-8 col-sm-8 field-input';
                    scope.largeTitleCss = 'control-label input-sm col-lg-1_5 col-md-2 col-sm-2 field-title';
                    scope.largeFieldCss = 'col-lg-10_5 col-md-10 col-sm-10 field-input';

                    var textareaId = [];

                    scope.textareaAutosize = function (id) {
                        if (!_.contains(textareaId, id)) {
                            $('#' + id).autosize();
                            textareaId.push(id);
                        }
                    };

                    scope.$on('$destroy', function () {
                        var i = 0;
                        for (; i < textareaId.length; i++) {
                            $('#' + textareaId[i]).autosize('autosize.destroy');
                        }
                    });

                    scope.inputChange = function (field) {
                        if (typeof scope.entity[field.name] !== 'undefined'
                            && typeof field.validation !== 'undefined'
                            && typeof field.validation.pattern !== 'undefined') {

                            if (field.validation.type === 'double') {
                                var scale = field.validation.pattern.substring(field.validation.pattern.indexOf('.') + 1).length;

                                if (scope.entity[field.name].indexOf('.') !== -1) {
                                    var splitted = scope.entity[field.name].split('.');
                                    if (splitted[1].length > scale) {
                                        scope.entity[field.name] = scope.entity[field.name].substring(0, scope.entity[field.name].length - 1);
                                    }
                                } else if (scope.entity[field.name].length > field.validation.pattern.indexOf('.')) {
                                    scope.entity[field.name] = scope.entity[field.name].substring(0, scope.entity[field.name].length - 1);
                                }
                            } else if (field.validation.type === 'long') {
                                if (scope.entity[field.name].length > field.validation.pattern.length) {
                                    scope.entity[field.name] = scope.entity[field.name].substring(0, scope.entity[field.name].length - 1);
                                }
                            }
                        }

                        if (scope.entity[field.name] === '') {
                            delete scope.entity[field.name];
                        }

                        if (typeof scope.entity[field.name] !== 'undefined'
                            && typeof scope.entity[field.name].id !== 'undefined') {
                            if (scope.entity[field.name].id === null || scope.entity[field.name].id === '') {
                                delete scope.entity[field.name];
                            }
                        }
                    };

                    scope.elm = {};

                    scope.inputClick = function (field, e) {
                        if (typeof field.validation !== 'undefined'
                            && typeof field.validation.length !== 'undefined'
                            && typeof scope.elm[field.name] === 'undefined') {
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
                                	//110为小键盘的小数点
                                    if (e.keyCode === 190 || e.keyCode === 110) {
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
                            scope.elm[field.name] = elm;
                        }
                    };

                    scope.getBooleanValue = function(options, val){
                        var index = _.findIndex(options, 'id', val + '');
                        if(index >= 0){
                            return options[index].name;
                        }
                        return '';
                    };
                }
            };
        }])

;