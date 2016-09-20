/**
 * Created by Jeffrey on 12/5/14.
 */
angular.module('shared.directives.uploader', [])
    .directive("fileread", [function () {
        return {
            scope: {
                fileread: "="
            },
            link: function (scope, element, attributes) {
                element.bind("change", function (changeEvent) {
                    scope.$apply(function () {
                        scope.fileread = changeEvent.target.files[0];
                        // or all selected files:
                        // scope.fileread = changeEvent.target.files;
                    });
                });
            }
        }
    }])
    .directive('uploader', [function () {
        return {
            restrict: 'A',
            scope: {

                // scope
                // define a new isolate scope
                action: '@',
                status: '=',
                domainName: '@'
            },
            controller: ['$scope', '$interval', 'DataService', function ($scope, $interval, DataService) {

                $scope.$on('uploader.closed', function () {
                    $scope.reset();
                });

                $scope.reset = function () {
                    $scope.elapsed = 0;
                    $scope.progress = 0;
                    $scope.uploadable = false;
                    $scope.uploading = false;
                    $scope.uploadLabel = '未上传';
                    $scope.processing = false;
                    $scope.processLabel = '未处理';
                    $scope.finished = false;
                    $scope.selectedFile = null;
                    $scope.resultLabel = '暂无结果';
                    $scope.result = null;
                    $scope.warning = false;
                    $scope.success = false;
                    $scope.download = false;
                    $scope.tooBig = false;
                    $scope.downloadUrl = null;

                    $('input[name="fileUpload"]').val('');
                    $('#uploadingInputId').val(0).trigger('change');
                    $('#processingInputId').val(0).trigger('change');
                };
                // controller:
                // here you should define properties and methods
                // used in the directive's scope
                $scope.elapsed = 0;
                $scope.progress = 0;
                $scope.uploadable = false;
                $scope.uploading = false;
                $scope.uploadLabel = '未上传';
                $scope.processing = false;
                $scope.processLabel = '未处理';
                $scope.finished = false;
                $scope.selectedFile = null;
                $scope.resultLabel = '暂无结果';
                $scope.result = null;
                $scope.warning = false;
                $scope.success = false;
                $scope.download = false;
                $scope.tooBig = false;
                $scope.downloadUrl = null;

                //$scope.hasSelected = false;
                //
                $scope.$watch('selectedFile', function (newValue, oldValue) {
                    if (newValue !== null) {
                        if (newValue.size > 20 * 1024 * 1024) {
                            $scope.resultLabel = '文件过大！';
                            $scope.warning = true;
                            $scope.tooBig = true;
                        } else {
                            $scope.uploadable = true;
                        }
                    }
                });

                $scope.$watch('result', function (newValue, oldValue) {
                    if (newValue !== null) {
                        $scope.success = newValue.success;
                        $scope.warning = !newValue.success;
                        $scope.status = false;

                        if ($scope.success) {
                            $scope.resultLabel = '导入成功';
                        } else if (newValue.resultInfo) {
                            $scope.resultLabel = newValue.resultInfo;
                        } else {
                            $scope.resultLabel = '导入失败';
                            $scope.downloadUrl = DataService.getImportFileUrl($scope.domainName, newValue.errorFileName);
                            $scope.download = true;
                        }
                    }
                });


                $scope.reselect = function () {
                    $scope.reset();
                    $('input[name="fileUpload"]').click();
                };

                var processInterval;

                var startTime;

                $scope.processIntervalFunc = function () {
                    if(!$scope.processing){
                        return;
                    }
                    var val = $('#processingInputId').val();

                    if (val >= 100) {
                        val = 10;
                    } else {
                        val = parseInt(val) + 10;
                    }

                    $('#processingInputId').val(val).trigger('change');
                };

                $scope.$on('$destroy', function () {
                    if (typeof processInterval !== 'undefined') {
                        $interval.cancel(processInterval);
                    }
                });

                $scope.sendFile = function () {
                    $scope.uploadable = false;
                    $scope.status = true;
                    var el = $('input[name="fileUpload"]');

                    var $form = el.parents('form');

                    if (el.val() == '') {
                        return false;
                    }

                    $form.attr('action', $scope.action);

                    //console.log($scope.action);

                    $scope.progress = 0;

                    $scope.uploading = true;
                    $scope.uploadLabel = '上传中';

                    startTime = new Date().getTime();
                    $form.ajaxSubmit({
                        type: 'POST',
                        uploadProgress: function (evt, pos, tot, percComplete) {
                            $scope.$apply(function () {
                                // upload the progress bar during the upload
                                $scope.progress = percComplete;
                                $('#uploadingInputId').val(percComplete).trigger('change');
                            });

                            if (100 === percComplete) {
                                $scope.$apply(function () {
                                    $scope.processing = true;
                                    $scope.uploadLabel = '已上传';
                                    $scope.processLabel = '处理中';
                                });
                                processInterval = $interval($scope.processIntervalFunc, 600);

                            }
                        },
                        error: function (evt, statusText, response, form) {

                            // remove the action attribute from the form
                            //$form.removeAttr('action');
                            //
                            //console.log(statusText)
                            ///*
                            // handle the error ...
                            // */
                            //$scope.$apply(function () {
                            //    $scope.uploading = false;
                            //});

                        },
                        success: function (response, status, xhr, form) {

                            //var ar = $(el).val().split('\\'),
                            //    filename = ar[ar.length - 1];

                            // remove the action attribute from the form
                            $form.removeAttr('action');

                            $scope.$apply(function () {
                                //$scope.avatar = filename;
                                $scope.processing = false;
                                $scope.finished = true;
                                $scope.result = response;$interval.cancel(processInterval);
                                $scope.processLabel = '已处理';
                                $('#processingInputId').trigger(
                                    'configure',
                                    {
                                        cursor: false
                                    }
                                );
                                $('#processingInputId').val(100).trigger('change');
                                $scope.elapsed = new Date().getTime() - startTime;
                            });
                            $scope.$emit('upload.success');
                        }
                    });
                }
            }],
            link: function (scope, elem, attrs, ctrl) {

                // link function
                // here you should register listeners
                //elem.find('.fake-uploader').click(function () {
                //    elem.find('input[type="file"]').click();
                //});

            },
            replace: false,
            templateUrl: 'app/shared/directives/uploader.tpl.html'
        };

    }]);