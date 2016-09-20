/**
 * Created by Jeffrey on 12/7/14.
 */
'use strict';

angular.module('shared.services.dialog', [])

    .factory('DialogService', [function () {
        //var init = $("#info_dialog").size > 0,
        //    elm = null,
        //    content = null;
        //
        //if (!init) {
        //    content = $('<div/>').attr('id', 'info_dialog_content');
        //    elm = $('<div/>').attr('id', 'info_dialog').addClass('hide')
        //        .append($('<div/>').height(200)
        //            .append(content));
        //}

        bootbox.setDefaults({
            /**
             * @optional String
             * @default: en
             * which locale settings to use to translate the three
             * standard button labels: OK, CONFIRM, CANCEL
             */
            locale: "zh_CN",
            /**
             * @optional Boolean
             * @default: true
             * whether the dialog should be shown immediately
             */
            show: true,
            /**
             * @optional Boolean
             * @default: true
             * whether the dialog should be have a backdrop or not
             */
            backdrop: true,
            /**
             * @optional Boolean
             * @default: true
             * show a close button
             */
            closeButton: false,
            /**
             * @optional Boolean
             * @default: true
             * animate the dialog in and out (not supported in < IE 10)
             */
            animate: false,
            /**
             * @optional String
             * @default: null
             * an additional class to apply to the dialog wrapper
             */
            className: "my-modal"
        });

        return {
            confirm: function (infos, callback) {
                var message = _(infos).join('<br>');

                return bootbox.confirm(message, function (result) {
                    if (result && typeof callback === 'function') {
                        callback();
                    }
                });
            },
            alert: function (infos, callback) {
                var message = _(infos).join('<br>');
                return bootbox.alert(message, function () {
                    if (typeof callback === 'function') {
                        callback();
                    }
                });
            },
            dialog: function (options) {
                options.size = "large";
                return bootbox.dialog(options);
            },
            form: function (form, options) {
                options.size = "large";
                return $(form).removeClass('hide').dialog(options);
            }
        };
    }])
;