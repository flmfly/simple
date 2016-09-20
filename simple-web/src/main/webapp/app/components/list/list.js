/**
 * Created by Jeffrey on 12/8/14.
 */

angular.module('components.list', [])


    .controller('ListCtrl', [
        '$scope',
        '$rootScope',
        '$window',
        'DataService',
        '$location',
        '$route',
        'Session',
        'DialogService',
        'GalleryService',
        'LoadingService',
        'APP_CONFIG',
        '$compile',
        function ($scope, $rootScope, $window, DataService,
                  $location, $route, Session, DialogService,
                  GalleryService, LoadingService,
                  APP_CONFIG, $compile) {

            var DEFAULT_PAGE_SIZES = [20, 50, 100];

            var DEFAULT_PAGE = {
                pageNumber: 1,
                pageSize: 20,
                sort: ''
            };

            $scope.debug = APP_CONFIG.DEBUG;

            $scope.query = Session.query === null ? {} : Session.query;
            Session.query = null;

            if (Session.property !== null) {
                $scope.query[Session.property] = {id: Session.propId};
            }

            $scope.page = Session.page === null ? DEFAULT_PAGE : Session.page;
            Session.page = null;

            $scope.templateUrl = DataService.getTemplateUrl(Session.domainName);
            $scope.originImportUrl = DataService.getImportUrl(Session.domainName);

            $scope.importUrl;

            $scope.getDomainDescFinished = false;
            $scope.hasUpdateImport = false;
            $scope.updateImprtLabel = '';

            var getCheckCell = function (id) {
                return '<span style="cursor:pointer;" class="fa fa-search-plus blue bigger-120" onclick="angular.element(\'[ng-view]\').scope().checkRow(' + id + ', event)"></span>';
            };

            var getEditCell = function (id, disabled) {
                return '<span style="cursor:pointer;" class="fa fa-pencil bigger-130 '
                    + (disabled ? 'grey"' : 'blue" onclick="angular.element(\'[name=' + Session.domainName + ']\').scope().editRow(' + id + ', event);"')
                    + '></span>';
            };

            var getDeleteCell = function (id, disabled) {
                return '<span style="cursor:pointer;" class="fa fa-trash red bigger-140" onclick="angular.element(\'[ng-view]\').scope().deleteRow(' + id + ', event)"></span>';
            };

            var getOperationCell = function (id, disabled, code, iconStyle) {
                return '<span style="cursor:pointer;" class="' + iconStyle + ' bigger-120 '
                    + (disabled ? 'grey"' : '" onclick="angular.element(\'[ng-view]\').scope().operate([\'' + id + '\'], \'' + code + '\', event)"')
                    + '></span>';
            };
            //
            //var getCell = function (id) {
            //    return '<span style="cursor:pointer;" class="fa fa-pencil bigger-140 blue" onclick="angular.element(\'[ng-jq-grid]\').scope().editRow(' + id + ', event);"></span>&nbsp;&nbsp;&nbsp;<span style="cursor:pointer;" class="fa fa-trash red bigger-140" onclick="angular.element(\'[ng-view]\').scope().deleteRow(' + id + ', event)"></span>';
            //};

            var pagingFinished = true;

            DataService.getDomainDesc(Session.domainName).then(function (response) {
                $scope.domainDesc = response.plain();
                if (typeof $scope.domainDesc.defaultSort !== 'undefined') {
                    $scope.page.sort = $scope.domainDesc.defaultSort;
                } else {
                    $scope.page.sort = '';
                }
                Session.setDomainDesc($scope.domainDesc);
                $scope.getDomainDescFinished = true;

                if (typeof $scope.domainDesc.updateImport !== 'undefined') {
                    $scope.hasUpdateImport = true;
                    $scope.updateImprtLabel = _(_.pluck($scope.domainDesc.updateImport, 'label')).join(' ');
                }

                $scope.booleanColumn = [];
                DataService.getTableDesc(Session.domainName).then(function (response) {
                    var columns = response.plain();
                    $scope.booleanColumn = _.filter(columns, function (n) {
                        return typeof n.booleanValue !== 'undefined';
                    });
                    var columnDef = [];

                    var canEdit = $scope.domainDesc.standarOperation.modify;
                    var canDel = $scope.domainDesc.standarOperation['delete'];
                    var canCheck = $scope.domainDesc.standarOperation['check'];

                    var operationNum = 0;

                    var multiOperationNum = 0;

                    if (canEdit) {
                        operationNum++;
                    }
                    if (canDel) {
                        operationNum++;
                        multiOperationNum++;
                    }

                    if (canCheck) {
                        operationNum++;
                    }

                    var operations = _.filter($scope.domainDesc.operation, {multi: false});

                    operationNum = operationNum + operations.length;
                    multiOperationNum = multiOperationNum + _.filter($scope.domainDesc.operation, {multi: true}).length;

                    var getCell = function (id, row) {
                        var cellStr = [];

                        if (canDel) {
                            cellStr.push(getDeleteCell(id));
                        }

                        var updateDisabled = false;
                        var _canEdit = canEdit;
                        for (i = 0; i < operations.length; i++) {
                            var operation = operations[i];
                            var disabled = typeof row[operation.code] !== 'undefined' && row[operation.code];
                            if (operation.code === 's_update') {
                                updateDisabled = disabled;
                                if (!canEdit) {
                                    _canEdit = true;
                                    operationNum++;
                                }
                                continue;
                            }
                            cellStr.push(getOperationCell(id, disabled, operation.code, operation.iconStyle));
                            operationNum++;
                        }

                        if (_canEdit) {
                            cellStr.unshift(getEditCell(id, updateDisabled));
                        }

                        if (canCheck) {
                            cellStr.unshift(getCheckCell(id));
                        }

                        return _(cellStr).join('&nbsp;&nbsp;&nbsp;')

                    };

                    var operateCellWidth = operationNum * 30;
                    if (operateCellWidth < 60) {
                        operateCellWidth = 60;
                    }

                    if (operationNum !== 0) {
                        columnDef.push({
                            label: '<span class="blue" style="position:relative;top:-6px">操作</span>',
                            name: 'act',
                            index: 'act',
                            width: operateCellWidth,
                            fixed: true,
                            align: 'center',
                            sortable: false,
                            resizable: false,
                            frozen: true,
                            formatter: function (cellvalue, options, rowObject) {
                                return getCell(rowObject.id, rowObject);
                            }
                        });
                    }

                    var i = 0;
                    for (; i < columns.length; i++) {
                        var tmp = columns[i];
                        if (typeof tmp.imageGallery !== 'undefined') {
                            (function (tmp) {

                                columnDef.push({
                                    label: tmp.title,
                                    name: tmp.name,
                                    index: tmp.name,
                                    align: 'center',
                                    sortable: false,
                                    resizable: false,
                                    formatter: function (cellvalue, options, rowObject) {
                                        return '<span style="cursor:pointer;" class="fa fa-'
                                            + (!tmp.imageGallery.isFileStyle ? 'photo' : 'paperclip')
                                            + ' bigger-140 blue" onclick="angular.element(this).scope().$parent.showGallery(event, \''
                                            + cellvalue + '\', \''
                                            + tmp.imageGallery.url
                                            + '\', \'' + rowObject.id
                                            + '\', \'' + tmp.imageGallery.fieldName + '\', \''
                                            + tmp.imageGallery.isFileStyle + '\', \''
                                            + tmp.imageGallery.fileNameProperty + '\');"></span>';
                                    }
                                });
                            }(tmp));
                        } else {
                            columnDef.push({
                                name: tmp.name,
                                index: tmp.name,
                                width: 150,
                                hidden: !tmp.show,
                                sortable: typeof tmp.sortable === 'undefined',
                                label: tmp.title
                            });
                        }
                    }

                    $scope.gridConfig = {
                        //data: '',
                        datatype: "custom",
                        cmTemplate: {title: false},
                        height: 350,
                        width: '100%',
                        shrinkToFit: false,
                        colModel: columnDef,
                        multiselect: multiOperationNum > 0,
                        headertitles: true,
                        //toppager:true,
                        viewrecords: true,
                        loadonce: false,
                        pagerpos: 'left',
                        //recordpos: 'center',
                        editurl: "dummy.html",
                        rowNum: $scope.page.pageSize,
                        rowList: $scope.pageSizes,
                        navConfig: {refresh: false, del: false, add: false, edit: false, search: false},
                        onPaging: function (pgButton) {
                            if (pagingFinished) {
                                pagingFinished = false;
                            } else {
                                return;
                            }
                            var $this = $(this);
                            setTimeout(function () {
                                var pageNumber = $this.jqGrid('getGridParam', 'page');
                                var lastPage = $this.jqGrid('getGridParam', 'lastpage');
                                if (pgButton === 'records') {
                                    pageNumber = 1;
                                }

                                if (pageNumber > lastPage) {
                                    DialogService.alert(['页码超出范围！']);
                                    $scope.$apply(function () {
                                        $scope.$broadcast('grid.data.update', {
                                            name: $scope.domain.domainName,
                                            d: $scope.page
                                        });
                                    });
                                    return false;
                                }
                                $this[0].grid.beginReq();
                                $scope.$apply(function () {

                                    if (pgButton === 'records') {
                                        $scope.page.pageNumber = 1;
                                    } else {
                                        $scope.page.pageNumber = $this.jqGrid('getGridParam', 'page');
                                    }

                                    $scope.page.pageSize = $this.jqGrid('getGridParam', 'rowNum');
                                    //$scope.$broadcast('refreshGrid');
                                });
                            }, 0);


                        },
                        onSortCol: function (index, columnIndex, sortOrder) {
                            var sort = index;

                            if (sortOrder == 'desc') {
                                sort = '-' + sort;
                            }

                            var $this = $(this);
                            setTimeout(function () {
                                $this[0].grid.beginReq();
                                $scope.$apply(function () {
                                    $scope.page.pageNumber = 1;
                                    $scope.page.sort = sort;
                                    //$scope.$broadcast('refreshGrid');
                                });
                            }, 0);
                        },
                        gridComplete: function (data) {
                            pagingFinished = true;
                        },
                        jsonReader: {
                            root: 'list',
                            page: 'pageNumber',
                            total: 'pageTotal',
                            records: 'total',
                            repeatitems: false
                        }
                    };

                    $scope.$watch('page.pageNumber + "_" + page.pageSize + "_" + page.sort', function () {
                        $scope.refreshGrid();
                    });
                });

            });

            $scope.importInProgress = false;

            $scope.importDialog;

            $scope.uploadSuccess = false;

            $scope.$on('upload.success', function () {
                $scope.uploadSuccess = true;
            });

            $scope.showImport = function (update) {
                if ($scope.getDomainDescFinished) {
                    $scope.uploadSuccess = false;
                    $scope.importUrl = $scope.originImportUrl + "?update=" + (update ? 'true' : 'false');

                    $scope.importDialog = DialogService.dialog({
                        title: $scope.domainDesc.label + ($scope.hasUpdateImport && update ? '更新' : '') + '导入 ' + ($scope.hasUpdateImport && update ? '（By ' + $scope.updateImprtLabel + '）' : ''),
                        message: $('#uploader').removeClass('hidden'),
                        //message : formElm.removeAttr('style'),
                        afterClose: function () {
                            $('#uploader').addClass('hidden');
                        },
                        buttons: {
                            cancel: {
                                label: '关闭',
                                className: 'btn-xs',
                                callback: function (event) {
                                    if ($scope.importInProgress) {
                                        var elm = $(event.target);
                                        elm.popover('destroy');
                                        elm.mouseout(function () {
                                            elm.popover('destroy');
                                        });

                                        elm.popover({
                                            placement: 'left',
                                            html: true,
                                            content: '<i class="fa fa-recycle blue"></i>&nbsp;&nbsp;<span class="red">导入进行中</span>'
                                        });
                                        elm.popover('show');
                                        //elm.blur();
                                        return false;
                                    }
                                    $scope.$broadcast('uploader.closed');
                                    if ($scope.uploadSuccess) {
                                        $scope.refreshGrid();
                                    }
                                }
                            }
                        }
                    });
                } else {
                    DialogService.alert(['信息获取中，请稍后...']);
                }
            };

            $scope.queryAreaToggle = {
                hideButtonName: '展开',
                showButtonName: '收起',
                style: {overflow: 'hidden', height: '0px'},
                isShown: false
            };

            $scope.operationParams = {};

            $scope.saveState = function () {
                Session.saveQueryParams($scope.query, $scope.page);
            };

            $scope.toggle = function () {
                $scope.queryAreaToggle.isShown = !$scope.queryAreaToggle.isShown;
                $scope.applyToggle();
            };

            $scope.applyToggle = function () {
                if (!$scope.queryAreaToggle.isShown) {
                    $scope.queryAreaToggle.style = {overflow: 'hidden', height: '0px'};
                } else {
                    $scope.queryAreaToggle.style = {};
                }
            };


            $scope.domain = {
                domainName: Session.domainName
            };

            $scope.operate = function (id, code, event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                $scope.saveState();
                postOperation(code, id);
            };

            var postOperation = function (code, ids) {
                var i = 0;
                for (; i < $scope.domainDesc.operation.length; i++) {
                    if ($scope.domainDesc.operation[i]['code'] === code) {
                        $scope.operation = $scope.domainDesc.operation[i];
                        break;
                    }
                }

                if ($scope.operation.parameters) {
                    DialogService.dialog({
                        title: '该操作您还需要输入以下信息',
                        message: $compile('<div id="operationPramDiv" class="row"><div form-fields entity="operationParams" fields="operation.parameters"></div></div>')($scope),
                        buttons: {
                            cancel: {
                                label: '关闭',
                                className: 'btn-xs',
                                callback: function (event) {
                                    $scope.operationParams = {};
                                }
                            },
                            submit: {
                                label: '提交',
                                className: 'btn-xs btn-success',
                                callback: function (event) {
                                    operationRequest(code, ids);
                                }
                            }
                        }
                    });
                } else {
                    operationRequest(code, ids);
                }

            };

            var operationRequest = function (code, ids) {
                DialogService.confirm(['您确定要进行操作吗？'], function () {
                    DataService.operate(Session.domainName, code, {
                        'ids': ids,
                        'parameters': $scope.operationParams
                    }).then(
                        function (response) {
                            var rtnData = response.plain();
                            if (rtnData.code === 200) {
                                DialogService.alert(['操作成功！'], function () {
                                    $scope.$broadcast('refreshGrid');
                                });
                            } else {
                                rtnData.messages.push('操作失败！');
                                DialogService.alert(rtnData.messages);
                            }
                        });
                }, true);
            };

            $scope.multiOperate = function (code, target) {
                var rowids = $('#grid-table-' + Session.domainName).jqGrid('getGridParam', 'selarrrow');

                if (rowids.length === 0 && target !== 'ALL') {
                    DialogService.alert(['您没有选择任何数据！']);
                } else {
                    postOperation(code, rowids);
                }
            };

            $scope.addRow = function () {
                $scope.saveState();
                Session.domainId = null;
                $location.path('/page/add/' + Session.domainName);
            };

            $scope.editRow = function (id, event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                $scope.saveState();
                $scope.$apply(function () {
                    $location.path('/page/edit/' + Session.domainName + "/" + id);
                });
            };

            $scope.deleteRow = function (id, event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }

                DialogService.confirm(['您确定要删除这条数据吗？'], function () {
                    DataService.remove(Session.domainName, id).then(function (response) {
                        var rtnData = response.plain();
                        if (rtnData.status.code === 200) {
                            DialogService.alert(['删除成功！'], function () {
                                $scope.$broadcast('refreshGrid');
                            });
                        } else {
                            rtnData.messages.push('删除失败！');
                            DialogService.alert(rtnData.messages);
                        }
                    });
                });
            };

            $scope.checkRow = function (id, event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }

                //if ($scope.fields) {
                showCheckDialog(id);
                //} else {
                //    DataService.getFormDesc(Session.domainName).then(function (response) {
                //        $scope.fields = response.plain();
                //        showCheckDialog(id);
                //    });
                //}
            };

            //$scope.fields;

            //$scope.checkEntity = {};

            //var element  = $compile('<div dynamic-form entity="checkEntity" domain="domain" readonly="true"></div>')($scope);

            var element;

            var showCheckDialog = function (id) {

                //var element = $compile('<div class="row"><div form-fields entity="checkEntity" readonly="true" fields="fields"></div></div>')($scope);


                DataService.get(Session.domainName, id).then(function (response) {
                    var rtnData = response.plain();

                    if (rtnData.status.code === 200) {
                        //$scope.checkEntity =

                        var scope = $scope.$new(true);

                        scope.checkEntity = rtnData.data;
                        scope.domain = $scope.domain;

                        element = $compile('<div dynamic-form entity="checkEntity" domain="domain" readonly="true"></div>')(scope);

                        //$scope.$broadcast('subList.init', $scope.checkEntity.id);

                        DialogService.dialog({
                            title: '数据查看',
                            message: element,
                            closeButton: true,
                            buttons: {
                                cancel: {
                                    label: '关闭',
                                    className: 'btn-xs'
                                }
                            }
                        });
                        setTimeout(function () {
                            $('input', $(element)).each(function (i, e) {
                                $(e).attr('disabled', 'true');
                            });
                            $('span', $('.form-group', $(element))).each(function (i, e) {
                                $(e).remove();
                            });
                        }, 100);
                        $scope.$broadcast('detail.dialog.opened');

                    }
                });
            };

            $scope.$on('detail.dialog.opened', function (event, data) {
                $(element).parent().attr('height', $('div', $(element)).height() + 'px');
                setTimeout(function () {
                    $('body').addClass('modal-open');
                    $scope.$broadcast("sub.show.dailog");
                    if ($('.modal-backdrop').height() < $('.modal-content').height() + 62) {
                        $('.modal-backdrop').height($('.modal-content').height() + 62);
                    }
                }, 200);
                $(window).resize();
            });

            $scope.delRows = function () {
                var rowids = $('#grid-table-' + Session.domainName).jqGrid('getGridParam', 'selarrrow');


                if (rowids.length === 0) {
                    DialogService.alert(['您没有选择任何数据！']);
                } else {
                    DialogService.confirm(['您确定要删除所选的数据吗？'], function () {
                        //var i = 0, ids = [];
                        //for (; i < $scope.gridOptions.selectedItems.length; i++) {
                        //    ids.push($scope.gridOptions.selectedItems[i].id);
                        //}
                        DataService.removeAll(Session.domainName, _(rowids).join()).then(
                            function (response) {
                                var rtnData = response.plain();
                                if (rtnData.code === 200) {
                                    DialogService.alert(['删除成功！'], function () {
                                        $scope.$broadcast('refreshGrid');
                                    });
                                } else {
                                    rtnData.messages.push('删除失败！');
                                    DialogService.alert(rtnData.messages);
                                }
                            });
                    }, true);
                }
            };

            $scope.showGallery = function (event, value, path, id, refName, isFileStyle, fileNameProperty) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                if (typeof path !== 'undefined') {
                    DataService.getSubList(Session.domainName, id, refName).then(function (response) {

                            var data = response.plain();

                            if (data.length === 0) {
                                DialogService.alert(['没有任何文件！']);
                            } else {
                                var i = 0;
                                for (; i < data.length; i++) {
                                    data[i][path] = DataService.getImageUrl(Session.domainName, data[i][path], data[i][fileNameProperty]);
                                }

                                if (isFileStyle === 'true') {
                                    GalleryService.file(data, path, fileNameProperty);
                                } else {
                                    GalleryService.image(data, {
                                        //urlProperty: 'data.urls[0]',
                                        urlProperty: path,
                                        carousel: false
                                    });
                                }
                            }

                        }
                    )
                    ;
                }
                else {
                    if (isFileStyle === 'true') {
                        //TODO
                        GalleryService.file([DataService.getImageUrl(Session.domainName, value)], path, fileNameProperty);
                    } else {
                        GalleryService.image([DataService.getImageUrl(Session.domainName, value)], {});
                    }
                }
            };

            $scope.pageSizes = DEFAULT_PAGE_SIZES;

            // Refresh the grid, calling the appropriate rest method.
            $scope.refreshGrid = function () {
                var reqPage = {
                    pageNumber: $scope.page.pageNumber,
                    pageSize: $scope.page.pageSize,
                    sort: $scope.page.sort
                };

                if (reqPage.pageNumber === 0) {
                    reqPage.pageNumber = 1;
                }

                $scope.$broadcast('spend.setStart');
                DataService.getPage(Session.domainName, $scope.query, reqPage)
                    .then(function (response) {
                        $scope.page = response.plain();
                        if ($scope.page.total === 0) {
                            $scope.page.pageNumber = 0;
                        }
                        if ($scope.page.total % $scope.page.pageSize === 0) {
                            $scope.page.pageTotal = $scope.page.total / $scope.page.pageSize;
                        } else {
                            $scope.page.pageTotal = Math.floor($scope.page.total / $scope.page.pageSize) + 1;
                        }
                        if ($scope.booleanColumn && $scope.booleanColumn.length > 0) {
                            processBooleanValue($scope.page);
                        }
                        $scope.$broadcast('grid.data.update', {name: $scope.domain.domainName, d: $scope.page});
                        $scope.$broadcast('spend.finished');
                    });
            };

            function processBooleanValue(page) {
                var data = page.list,
                    i = 0;

                for (; i < data.length; i++) {
                    var j = 0;
                    for (; j < $scope.booleanColumn.length; j++) {
                        var val = data[i][$scope.booleanColumn[j].name];
                        if (val === undefined || val === "") {
                            data[i][$scope.booleanColumn[j].name] = "";
                        } else if (val) {
                            data[i][$scope.booleanColumn[j].name] = $scope.booleanColumn[j].booleanValue[0];
                        } else {
                            data[i][$scope.booleanColumn[j].name] = $scope.booleanColumn[j].booleanValue[1];
                        }
                    }
                }
            }

            $scope.reset = function () {
                $route.reload();
            };

            $scope.queryPage = function () {
                if ($scope.page.pageNumber == 1) {
                    $scope.refreshGrid();
                } else {
                    $scope.page.pageNumber = 1;
                }
            };

            $scope.$on('refreshGrid', function () {
                $scope.refreshGrid();
            });

            $scope.exportExcel = function () {
                var form = $("<form>");//定义一个form表单
                form.attr("style", "display:none");
                form.attr("target", "_blank");
                form.attr("method", "post");
                form.attr("action", DataService.getExportUrl(Session.domainName));
                var input1 = $("<input>");
                input1.attr("type", "hidden");
                input1.attr("name", "query");
                input1.attr("value", JSON.stringify($scope.query));
                $("body").append(form);//将表单放置在web中
                form.append(input1);

                form.submit();
                form.remove();
            };

            $scope.$on('$destroy', function () {
                if (typeof $scope.importDialog !== 'undefined') {
                    $scope.importDialog.remove();
                }
            });

        }])

;