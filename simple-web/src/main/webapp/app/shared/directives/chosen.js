/**
 * Created by Jeffrey on 12/10/14.
 *
 * Events
 * 1.
 */

angular.module('shared.directives.chosen', [])

    .directive('chosen', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            template: '<select class="chosen-select form-control input-sm" ng-model="selectedVal" ng-change="selectedChanged()" ng-options="item as item[valParam] for item in items track by item.id"><option value=""></option></select>',
            replace: true,
            scope: {
                val: '=',
                items: '=',
                valParam: '@',
                diseditable: '=',
                type: '='
            },
            link: function (scope, element, attrs) {

                scope.$watch('val', function (newValue) {
                    if (typeof newValue === 'undefined') {
                        scope.selectedVal = null;
                    } else {
                        if(scope.type === 'enum'){
                            scope.selectedVal = {id: newValue, name: newValue};
                        }else{
                            scope.selectedVal = newValue;
                        }
                    }
                }, true);

                scope.$watch('selectedVal', function (newValue) {
                    delete scope.val;
                    if (newValue !== null) {
                        if(scope.type === 'enum'){
                            scope.val = newValue.id;
                        }else {
                            scope.val = newValue;
                        }
                        element.val(newValue.id);
                    } else {
                        element.val('');
                    }
                    element.trigger("chosen:updated");
                }, true);

                //scope.$on('sub.add.start', function () {
                //    if (typeof scope.val === 'undefined') {
                //        scope.selectedVal = null;
                //    } else {
                //        scope.selectedVal = scope.val;
                //    }
                //});

                //scope.selectedChanged = function () {
                //    delete scope.val;
                //    if (typeof scope.selectedVal.id !== 'undefined') {
                //        scope.val = scope.selectedVal;
                //    }
                //};
                scope.$watch('items', function (newValue, oldValue) {
                    //console.log(newValue);
                    if (typeof newValue === 'undefined' || newValue.length === 0) {
                        return;
                    }

                    var config = {
                        no_results_text: '未找到结果',
                        placeholder_text_multiple: 'Select Some Options',
                        placeholder_text_single: 'Select an Option'
                    };

                    if (typeof attrs.deselected !== 'undefined' && attrs.deselected === 'true') {
                        config.allow_single_deselect = true;
                    }

                    if (scope.diseditable) {
                        $(element).attr('disabled', 'disabled');
                    }

                    $(element).attr('data-placeholder', attrs.placeholder)
                        .chosen(config);

                    if (typeof attrs.width !== 'undefined') {
                        $(element).next().css({'width': attrs.width});
                    } else {
                        //$(element).next().css({'width': '100%'});
                        $(window)
                            .off('resize.chosen')
                            .on('resize.chosen', function () {
                                $('.chosen-select').each(function () {
                                    var $this = $(this);
                                    $this.next().css({'width': $this.parent().width()});
                                })
                            }).trigger('resize.chosen');
                        //resize chosen on sidebar collapse/expand
                        $(document).on('settings.ace.chosen', function (e, event_name, event_val) {
                            if (event_name != 'sidebar_collapsed') return;
                            $('.chosen-select').each(function () {
                                var $this = $(this);
                                $this.next().css({'width': $this.parent().width()});
                            })
                        });
                    }
                }, true);
            }
        }
    }])
;