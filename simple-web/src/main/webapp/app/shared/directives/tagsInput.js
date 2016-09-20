/**
 * Created by Jeffrey on 12/10/14.
 *
 * Events
 * 1.
 */

angular.module('shared.directives.tagsInput', [])

    .directive('tagsInput', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            template: '<input name="tags" value="" type="text" placeholder="{{field.type.placeholder}}"/>',
            replace: true,
            scope: {
                entity: '=',
                field: '='
            },
            link: function (scope, element, attrs) {
                var tagsChanged = function () {
                    var val = element.val();
                    if (val === '') {
                        delete scope.entity[scope.field.name];
                    } else {
                        scope.entity[scope.field.name] = val;
                    }
                };

                try {
                    element.tag();
                    element.on('added', function (e, value) {
                        tagsChanged();
                    });
                    element.on('removed', function (e, value) {
                        tagsChanged();
                    });
                    element.parent().attr('style', 'width:100%');
                    element.next().attr('style', 'width:100%');
                }
                catch (e) {
                }
            }
        }
    }])
;