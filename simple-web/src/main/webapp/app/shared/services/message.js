'use strict';

angular.module('shared.services.message', [])
    .service('MessageService', ['DataService', '$rootScope', function (DataService, $rootScope) {
        var self = this;
        this.messages = [];
        this.alarms = [];
        this.lastQueryTime = -1;
        this.minQueryGap = 60 * 1000;
        this.maxId = -1;

        this.getMessages = function () {
            return _.clone(this.messages);
        };

        this.getAlarms = function () {
            return _.clone(this.alarms);
        };

        $rootScope.$on('request.trigger', function () {
            var now = new Date().getTime();


            if (now - self.lastQueryTime > self.minQueryGap) {
                self.lastQueryTime = now;
                DataService.getMessage(self.maxId).then(function (response) {
                    var rtn = response.plain();
                    if (rtn.status.code === 200 && typeof rtn.data !== 'undefined') {
                        if (rtn.data.length > 0) {
                            var i = rtn.data.length - 1;
                            self.maxId = rtn.data[i].deliveryId;

                            for (; i >= 0; i--) {
                                var msg = rtn.data[i];
                                if (msg.type === 'ALARM') {
                                    self.alarms.push(msg);
                                } else {
                                    self.messages.push(msg);
                                }
                            }

                            $rootScope.$broadcast('message.refreshed');
                        }
                    }
                });
            }
        });

        $rootScope.$on('message.read', function (event, data) {
            var removed = _.remove(self.messages, function (n) {
                return n.deliveryId === data;
            });

            if (removed.length === 0) {
                removed = _.remove(self.alarms, function (n) {
                    return n.deliveryId === data;
                });
            }

            if (removed.length > 0) {
                DataService.setMessageRead(_.pluck(removed, 'deliveryId').join());
            }

        });

    }]);