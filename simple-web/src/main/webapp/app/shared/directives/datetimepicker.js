/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('shared.directives.datetimepicker', [])

    .directive('datetimepicker', ['APP_CONFIG', function (APP_CONFIG) {
        return {
            restrict: 'A',
            templateUrl: 'app/shared/directives/datetimepicker.tpl.html',
            replace: true,
            scope: {
                val: '=',
                type: '=',
                disabled: '=',
                placeholder: '=',
                identifier: '@',
                position: '@',
                appendEndOfDay: '@'
            },
            link: function (scope, element, attrs) {
                var self = this;
                var entityChanged = false;
                var viewChanged = false;

                if (!scope.disabled) {
                    scope.viewVal = '';
                    scope.isDate = scope.type === 'date';
                    var elm = $('input', $(element));

                    scope.readonly = false;

                    var format;
                    if (scope.type === 'datetime') {
                        format = APP_CONFIG.DATETIME_FORMAT;
                    } else if (scope.type === 'date') {
                        format = APP_CONFIG.DATE_FORMAT;
                    } else if (scope.type === 'time') {
                        format = APP_CONFIG.TIME_FORMAT;
                    }

                    elm.datetimepicker({
                        locale: 'zh-cn',
                        //showClose: true,
                        useCurrent: false,
                        sideBySide: true,
                        format: format
                    }).on('dp.change', function (ev) {
                        self.entityChanged = false;
                        scope.$apply(function () {
                            scope.viewVal = elm.val();
                        });

                        if (scope.identifier && ev.date) {
                            if (scope.position === 'left') {
                                $('input', '#datetimepicker_r_' + scope.identifier).data("DateTimePicker").minDate(ev.date);

                                if ($('input', '#datetimepicker_r_' + scope.identifier).data("DateTimePicker").date()
                                    && $('input', '#datetimepicker_r_' + scope.identifier).data("DateTimePicker").date()._d.getTime() < elm.data("DateTimePicker").date()._d.getTime()) {
                                    $('input', '#datetimepicker_r_' + scope.identifier).val('');
                                    $('input', '#datetimepicker_r_' + scope.identifier).data("DateTimePicker").clear();
                                }
                            } else if (scope.position === 'right') {
                                $('input', '#datetimepicker_l_' + scope.identifier).data("DateTimePicker").maxDate(ev.date);
                                if ($('input', '#datetimepicker_l_' + scope.identifier).data("DateTimePicker").date()
                                    && $('input', '#datetimepicker_l_' + scope.identifier).data("DateTimePicker").date()._d.getTime() > elm.data("DateTimePicker").date()._d.getTime()) {
                                    $('input', '#datetimepicker_l_' + scope.identifier).val('');
                                    $('input', '#datetimepicker_l_' + scope.identifier).data("DateTimePicker").clear();
                                }
                            }
                        }


                    }).on('dp.hide', function (ev) {
                        ev.target.blur();
                    }).on('dp.show', function (ev) {
                    });
                    elm.keydown(function (e) {
                        //e.preventDefault();
                    });
                    elm.next().on(ace.click_event, function () {
                        $(this).prev().focus();
                    });
                    elm.next().next().on(ace.click_event, function () {
                        elm.val('');
                        elm.data("DateTimePicker").clear();

                        if (scope.identifier) {
                            if (scope.position === 'left') {
                                $('input', '#datetimepicker_r_' + scope.identifier).data("DateTimePicker").minDate(false);
                            } else if (scope.position === 'right') {
                                $('input', '#datetimepicker_l_' + scope.identifier).data("DateTimePicker").maxDate(false);
                            }
                        }

                        scope.$apply(function () {
                            delete scope.val;
                        });
                    });
                    scope.$watch('viewVal', function (newValue, oldValue) {
                        if (self.entityChanged) {
                            self.entityChanged = false;
                            return;
                        }

                        self.viewChanged = true;
                        if (newValue === '') {
                            delete scope.val;
                        } else {
                            if (scope.isDate) {
                            	console.log(scope.appendEndOfDay);
                            	if(scope.appendEndOfDay && scope.appendEndOfDay === 'true'){
                            		scope.val = newValue + ' 23:59:59';
                            	}else{
                            		scope.val = newValue + ' 00:00:00';
                            	}
                            } else {
                                scope.val = newValue;
                            }
                        }
                    });

                }

                if (typeof scope.val !== 'undefined') {
                    if (scope.isDate) {
                        scope.viewVal = scope.val.substr(0, 10);
                    } else {
                        scope.viewVal = scope.val;
                    }
                }

                //scope.$on('datetime.changed', function(e, d){
                //    if(typeof scope.val !== 'undefined'){
                //        scope.viewVal = scope.val;
                //    }
                //});

                scope.$watch('val', function (newValue, oldValue) {
                    if (self.viewChanged) {
                        self.viewChanged = false;
                        return;
                    }
                    if (typeof newValue !== 'undefined') {
                        self.entityChanged = true;
                        if (scope.isDate) {
                            newValue = newValue.substr(0, 10);
                        }
                        scope.viewVal = newValue;
                    } else {
                        scope.viewVal = '';
                    }
                });
            }
        }
    }])
;