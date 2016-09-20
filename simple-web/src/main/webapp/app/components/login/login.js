/**
 * Created by Jeffrey on 12/25/14.
 */
'use strict';

angular.module('components.login', ['shared.services.data', 'shared.services.dialog', 'shared.services.loading'])

    .controller('LoginController', ['$scope', 'DataService', 'DialogService', function ($scope, DataService, DialogService) {
//        $scope.verifyCodeUrl = DataService.getVerifyCodeUrl() + '?_requestTime=' + new Date().getTime();

        $scope.user = {account: '', password: '', newPassword: '', password1: ''};

        var confirmPasswordCorrectCss = "fa-check green";

        var confirmPasswordIncorrectCss = "fa-close red";

        var input_account = $('#input_account');
        input_account.focus();
        var input_password = $('#input_password');
        var input_verifyCode = $('#input_verifyCode');
        var button_login = $('#button_login');

        $scope.confirmPasswordCss = '';

        $scope.refreshVerifyCode = function () {
            $scope.verifyCodeUrl = DataService.getVerifyCodeUrl() + '?_requestTime=' + new Date().getTime();
        };

        $scope.keyDown = function (e) {
            if (e.keyCode === 13) {
                var targetId = e.target.id;

                if (targetId === input_account.attr('id')) {
                    input_password.focus();
                } else if (targetId === input_password.attr('id')) {
                    if (!input_verifyCode.is(':hidden')) {
                        input_verifyCode.focus();
                    } else {
                        button_login.focus();
                        button_login.click();
                    }
                }else if (targetId === input_verifyCode.attr('id')) {
                        button_login.focus();
                        button_login.click();
                }
            }
        };

        $scope.login = function ($event) {
            $event.preventDefault();
            DataService.login($scope.user).then(
                function (response) {
                    var rtnData = response.plain();
                    if (rtnData.status.code === 200) {
                        window.location.href = 'index.html';
                    } else {
                        $scope.error = rtnData.infos.join('\n');
                        if (rtnData.data.needVerify) {
                            $scope.refreshVerifyCode();
                            delete rtnData.data.verifyCode;
                        }
                    }
                    $scope.user = rtnData.data;
                });
            return false;
        };

        $scope.clearError = function () {
            delete $scope.error;
            //$scope.user = {};
        };

        $scope.changePassword = function () {
            // check fields
            var errors = [];
            // check username
            if ($scope.user.account === '') {
                errors.push('用户名不能为空！');
            }

            // check password
            if ($scope.user.password === '') {
                errors.push('密码不能为空！');
            }

            // check new password
            if ($scope.user.newPassword === '') {
                errors.push('新密码不能为空！');
            } else if($scope.user.newPassword.length < 6){
            	errors.push('新密码不能小于6位！');
//            } else if(!(/[a-z]+/.test($scope.user.newPassword)
//            		&& /[A-Z]+/.test($scope.user.newPassword) 
//            		&& /\d+/.test($scope.user.newPassword))){
//            	errors.push('新密码必须含有大小写字母和数字！');
            } else if($scope.user.newPassword !== $scope.user.password1){
            	errors.push('新密码和确认密码不一致！');
            }

            if (errors.length > 0) {
                $scope.error = errors.join('\n');
            } else {
                DataService.changePassword($scope.user).then(function (response) {
                    var rtnData = response.plain();

                    if (rtnData.status.code === 200) {
                        DialogService.alert(['密码修改成功！']);
                        $scope.user.password = '';
                        $scope.user.newPassword = '';
                        $scope.user.password1 = '';
                        delete $scope.error;
                    } else {
                        $scope.error = rtnData.infos.join('\n');
                    }
                });
            }
        };

        $scope.reset = function () {
            $scope.user.account = '';
            $scope.user.password = '';
            $scope.user.newPassword = '';
            $scope.user.password1 = '';
        };

        $scope.onNewPasswordInput = function () {
            var css = "";

            if ($scope.user.newPassword !== $scope.user.password1) {
                css = css + " " + confirmPasswordIncorrectCss;
            } else if ($scope.user.newPassword !== '') {
                css = css + " " + confirmPasswordCorrectCss;
            }


            $scope.confirmPasswordCss = css;
        };
    }])

;