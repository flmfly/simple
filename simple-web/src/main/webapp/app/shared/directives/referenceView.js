/**
 * Created by Jeffrey on 12/10/14.
 *
 * Events
 * 1.
 */

angular.module('shared.directives.referenceView', [])

    .directive('referenceView', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            template: '<input type="text" class="form-control input-sm" ng-readonly="true" />',
            replace: true,
            scope: {
                target: '=',
                path: '='
            },
            link: function (scope, element, attrs) {
                var splited = scope.path.split('.');
                scope.$watch('target', function (newValue, oldValue) {
                    var i = 1;
                    var obj = null;
                    if (typeof newValue !== 'undefined' && newValue != null) {
                        if (typeof newValue[splited.join('_')] !== 'undefined') {
                            element.val(newValue[splited.join('_')]);
                        } else {
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
                                element.val(obj);
                            }
                        }
                    } else {
                        element.val('');
                    }
                });

            }
        }
    }])
;