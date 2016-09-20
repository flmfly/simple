/**
 * Created by Jeffrey on 12/10/14.
 *
 * Events
 * 1.
 */

angular.module('shared.directives.messageView', [])

    .directive('messageView', ['MessageService', 'DialogService', function (MessageService, DialogService) {
        return {
            restrict: 'A',
            templateUrl: 'app/shared/directives/messageView.tpl.html',
            replace: false,
            controller: function ($scope) {
                $scope.messages = [];
                $scope.alarms = [];
            },
            scope: {},
            link: function (scope, element, attrs) {

                scope.$on('message.refreshed', function () {
                    scope.messages = MessageService.getMessages();
                    scope.alarms = MessageService.getAlarms();

                    scope.totalMessage = scope.messages.length;
                    scope.totalAlarm = scope.alarms.length;
                });

                scope.showMessage = function (id, event) {
                    var msg;
                    var index = _.findIndex(scope.messages, function (chr) {
                        return chr.deliveryId === id;
                    });

                    if (index === -1) {
                        index = _.findIndex(scope.alarms, function (chr) {
                            return chr.deliveryId === id;
                        });
                        msg = scope.alarms[index];
                    } else {
                        msg = scope.messages[index];
                    }

                    var dialog = DialogService.dialog({
                        title: '详细内容',
                        message: '<div><h4 class="center">' + msg.title + '</h4><p>' + msg.content + '</p></div>',
                        buttons: {
                            ok: {
                                label: '<i class="ace-icon fa fa-check"></i> OK',
                                className: 'btn-xs btn-success',
                                callback: function () {
                                    var removed = _.remove(scope.messages, function (n) {
                                        return n.deliveryId === id;
                                    });

                                    if (removed.length === 0) {
                                        _.remove(scope.alarms, function (n) {
                                            return n.deliveryId === id;
                                        });
                                    }
                                    scope.$emit('message.read', id);
                                    $(event.target).closest('li').remove();
                                    scope.$digest();
                                }
                            }
                        }
                    });
                };
            }
        }
    }])
;