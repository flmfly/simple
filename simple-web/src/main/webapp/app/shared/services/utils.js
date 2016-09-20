/**
 * Created by Jeffrey on 12/7/14.
 */
'use strict';

angular.module('shared.services.utils', [])

    .service('UtilsService', [function () {
        this.getValue = function (target, path) {
            if (!path) {
                return undefined;
            }
            var split = path.split('.');

            if (typeof target === 'undefined') {
                return undefined;
            }

            if (null == target) {
                if (split.length === 1) {
                    return null;
                } else {
                    return undefined;
                }
            }

            var i = 1;
            var obj = target[split[0]];

            if (obj) {
                for (; i < split.length; i++) {
                    obj = obj[split[i]];
                    if (!obj) {
                        break;
                    }
                }
            }

            if (obj == null && i < split.length - 1) {
                obj = undefined;
            }

            return obj;
        };
    }]);