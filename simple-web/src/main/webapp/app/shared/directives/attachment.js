/**
 * Created by Jeffrey on 12/10/14.
 */

angular.module('shared.directives.attachment', [])

    .directive('attachment', ['DataService', 'Session', 'DialogService', function (DataService, Session, DialogService) {
        return {
            restrict: 'A',
            templateUrl: 'app/shared/directives/attachment.tpl.html',
            replace: true,
            scope: {
                field: '=',
                entity: '='
            },
            link: function (scope, element, attrs) {
                scope.selectFiles = function () {
                    $('input', element).click();

                };

                scope.checkDimension = scope.field.type.attachment.height && scope.field.type.attachment.height > 0 && scope.field.type.attachment.width && scope.field.type.attachment.width > 0;

                scope.format = (scope.field.type.attachment.type === '' ? '无限制' : scope.field.type.attachment.type);
                scope.maxSize = Math.floor(scope.field.type.attachment.maxSize / 1024) + ' KB';
                scope.dimension = scope.checkDimension ? scope.field.type.attachment.width + 'x' + scope.field.type.attachment.height : '';

                scope.queue = [];

                scope.initData = function (entry) {
                    if (typeof entry.id !== 'undefined') {
                        DataService.getSubList(Session.domainName, entry.id, scope.field.name).then(function (response) {
                            var dataList = response.plain();
                            var i;

                            for (i = 0; i < dataList.length; i++) {
                                scope.queue.push(scope.transformData(dataList[i], scope.field.type.attachment));
                            }

                            scope.entity[scope.field.name] = dataList;

                            setTimeout(function () {
                                scope.$emit('sub.init.finished', scope.field.name);
                            }, 0);
                        });
                    } else {
                        setTimeout(function () {
                            scope.$emit('sub.init.finished', scope.field.name);
                        }, 0);
                    }
                };

                scope.transformData = function (item, desc) {
                    return {
                        url: DataService.getImageUrl(Session.domainName, item[desc.url], item[desc.fileName]),
                        fileName: item[desc.fileName],
                        size: Math.floor(item[desc.size] / 1024) + ' Kb',
                        id: item.id
                    };
                };

                scope.$on('domain.saved', function (event, data) {
                    scope.queue = [];
                    scope.initData(data);
                });

                scope.transformQueue = function (item, desc) {
                    var rtn = {};
                    rtn[desc.url] = item.url;
                    rtn[desc.fileName] = item.fileName;
                    rtn[desc.size] = item.size;
                    return rtn;
                };

                scope.removeAttachment = function (file) {
                    var i = 0;
                    for (; i < scope.queue.length; i++) {
                        if (file === scope.queue[i]) {
                            break;
                        }
                    }

                    scope.queue.splice(i, 1);
                    scope.entity[scope.field.name].splice(i, 1);
                    //TODO: also remove file from server side
                };

                scope.initData(scope.entity);

                this.validateFile = function (file) {
                    if (scope.field.type.attachment.type.indexOf(file.type) < 0) {

                    }
                };

                $('input', element).fileupload({
                    dataType: 'json',
                    url: DataService.getUploadUrl(Session.domainName),
                    change: function (e, data) {
                        var errorMsg = [];
                        $.each(data.files, function (index, file) {

                            if (scope.field.type.attachment.type != "" && (file.type == ""|| scope.field.type.attachment.type.indexOf(file.type) < 0)) {
                                errorMsg.push(file.name + '文件格式不正确!');
                            }
                            if (file.size > scope.field.type.attachment.maxSize) {
                                errorMsg.push(file.name + '文件过大(' + Math.floor(file.size / 1024) + ' KB)!');
                            }
                        });

                        if (errorMsg.length > 0) {
                            DialogService.alert(errorMsg);
                            return false;
                        }
                    },
                    done: function (e, data) {
                        //$("tr:has(td)").remove();

                        if (typeof scope.entity[scope.field.name] === 'undefined') {
                            scope.entity[scope.field.name] = [];
                        }

                        var errorMsg = [];

                        $.each(data.result, function (i, d) {
                            scope.entity[scope.field.name].push(scope.transformQueue(d, scope.field.type.attachment));

                            d.url = DataService.getImageUrl(Session.domainName, d.url, d.fileName);
                            d.size = Math.floor(d.size / 1024) + ' KB';

                            var push = true;

                            if (scope.checkDimension) {
                                if (d.height > scope.field.type.attachment.height ||
                                    d.width > scope.field.type.attachment.width) {
                                    errorMsg.push(d.fileName + ' 尺寸太大(' + d.width + 'x' + d.height + '),保存失败!');
                                    push = false;
                                }
                            }

                            if (push) {
                                scope.queue.push(d);
                            }else{
                                scope.entity[scope.field.name].length--;      
                            }
                        });

                        if (errorMsg.length > 0) {
                            DialogService.alert(errorMsg);
                        }

                        scope.$digest();
                        //$.each(data.result, function (index, file) {
                        //
                        //    $("table", element).append(
                        //        $('<tr/>').css('border-bottom', 'solid 1px blue')
                        //            .append($('<td/>').text(file.fileName))
                        //            .append($('<td/>').text(file.fileSize))
                        //            //.append($('<td/>').text(file.fileType))
                        //            .append($('<td/>').html("<a href='rest/controller/get/" + index + "'><i class='fa fa-download bigger-160'></i></a>"))
                        //            .append($('<td/>').html("<a href='rest/controller/get/" + index + "'><i class='fa fa-remove red bigger-200'></i></a>"))
                        //    )//end $("#uploaded-files").append()
                        //});
                        setTimeout(function () {
                            $('.ui-progressbar', element).addClass('hidden');
                        }, 500);

                    },

                    start: function (e) {
                        //console.log(111)
                        $('.ui-progressbar-value', element).css(
                            'width',
                            '0%'
                        );
                        $('.ui-progressbar', element).removeClass('hidden');
                    },

                    progressall: function (e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        $('.ui-progressbar-value', element).css(
                            'width',
                            progress + '%'
                        );
                    }
                });
            }
        }
    }])
;