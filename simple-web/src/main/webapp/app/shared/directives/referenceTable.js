/**
 * Created by Jeffrey on 12/5/14.
 */
angular.module('shared.directives.referenceTable', [])

    .constant('DEFAULT_PAGE_SIZES', [20, 50, 100])

    .constant('REFERENCE_TABLE_DEFAULT_PAGE', {
        pageNumber: 1,
        pageSize: 20,
        sort: ''
    })

    .directive('referenceTable', ['UtilsService', 'DataService', 'DialogService', 'Session', 'REFERENCE_TABLE_DEFAULT_PAGE', 'DEFAULT_PAGE_SIZES',
        function (UtilsService, DataService, DialogService, Session, REFERENCE_TABLE_DEFAULT_PAGE, DEFAULT_PAGE_SIZES) {
            return {
                restrict: 'A',
                templateUrl: 'app/shared/directives/referenceTable.html',
                replace: true,
                scope: {
                    entity: '=',
                    field: '=',
                    fields: '=',
                    domain: '=',
                    disabled: '='
                },
                controller: function ($scope) {
                    //$scope.debug = APP_CONFIG.DEBUG;

                    var self = this;
                    var refDomainName = $scope.field.type.refDomainName;

                    $scope.id = new Date().getTime();

                    var formNameStr = '#ref-' + $scope.field.name.split('.').join('_') + '_' + $scope.id;
                    var jqgridNameStr = $scope.field.name.split('.').join('_') + "_" + refDomainName + "_ref_" + $scope.id;

                    this.initVal = function (d) {
                        if (typeof d[$scope.field.type.refName] !== 'undefined') {
                            $scope.queryClass = 'fa-check green';
                            $scope.input = {val: UtilsService.getValue(d[$scope.field.type.refName], $scope.field.type.refLabel)};
                        } else {
                            $scope.input = {val: ''};
                            $scope.queryClass = '';
                        }
                    };

                    self.initVal($scope.entity || {});

                    $scope.shouldHide = function (type) {
                        return type.refName === Session.parent.prop || $scope.disabled;
                    };


                    //entity[field.type.refName] ? 'fa-check green':'fa-exclamation red'
                    // ------------------------------------ for select table start ---------------------------

                    $scope.refDomain = {
                        domainName: $scope.field.type.refDomainName
                    };

                    $scope.$on('editable.reference.changed', function (e, d) {
                        self.initVal(d);
                    });

                    $scope.query = {};

                    $scope.depend = $scope.field.type.depend;
                    $scope.refDomainName = $scope.field.type.refDomainName;
                    $scope.dependAssociateField = $scope.field.type.dependAssociateField;

                    $scope.dependVal = {};

                    $scope.page = REFERENCE_TABLE_DEFAULT_PAGE;

                    $scope.queryAreaToggle = {
                        hideButtonName: '展开',
                        showButtonName: '收起',
                        style: {overflow: 'hidden', height: '0px'},
                        isShown: false
                    };

                    if (typeof $scope.depend !== 'undefined' && $scope.depend.indexOf('.') != -1) {
                        $scope.$emit('object.dependency.found', $scope.depend);
                    }

                    var stopInput = 0;
                    var ins = null;
                    var refName = $scope.field.type.refName;
                    if ($scope.field.name.indexOf('.') !== -1) {
                        refName = $scope.field.name;
                    }
                    $scope.queryForInput = function () {

                        stopInput = new Date().getTime();

                        if ($scope.input.val === '') {
                            delete $scope.entity[refName];
                            $scope.queryClass = '';
                            return;
                        }

                        $scope.queryClass = 'fa-history orange2';
                        if (!ins) {
                            ins = setInterval(function () {
                                if (new Date().getTime() - stopInput > 500) {
                                    $scope.fireQuery();
                                    clearInterval(ins);
                                    ins = null;
                                }
                            }, 100);
                        }
                    };

                    $scope.fireQuery = function () {
                        if (!$scope.dependCheck() || $scope.input.val === "") {
                            return;
                        }
                        var reqPage = {
                                pageNumber: 1,
                                pageSize: 1,
                                sort: ''
                            },
                            queryObj = {},
                            refName = $scope.field.type.refName;

                        if ($scope.field.name.indexOf('.') !== -1) {
                            refName = $scope.field.name;
                        }

                        queryObj[$scope.field.type.refLabel] = {val: $scope.input.val};

                        if ($scope.depend) {
                            queryObj[$scope.dependAssociateField] = $scope.dependVal;
                        }

                        DataService.getPage(refDomainName, queryObj, reqPage)
                            .then(function (response) {
                                var rtn = response.plain();
                                if (rtn.total === 1) {
                                    DataService.get(refDomainName, rtn.list[0].id).then(function (response) {
                                    	$scope.entity[refName] = response.plain().data;
                                        //$scope.inputVal = $scope.entity[refName][$scope.field.type.refLabel];
                                    });
                                    $scope.queryClass = 'fa-check green';
                                } else {
                                    $scope.queryClass = 'fa-exclamation red';
                                    delete $scope.entity[$scope.field.type.refName];
                                }
                            });
                    };

                    $scope.clear = function (type) {
                        if (type.refName === Session.parent.prop) {
                            return;
                        }

                        var refName = $scope.field.type.refName;
                        if ($scope.field.name.indexOf('.') !== -1) {
                            refName = $scope.field.name;
                        }

                        delete $scope.entity[refName];
                        $scope.input = {val: ''};
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
                        setTimeout(function () {
                            //$('body').addClass('modal-open');

                            if ($('.modal-backdrop', $($scope.dialog)).height() < $('.modal-content', $($scope.dialog)).height() + 62) {
                                $('.modal-backdrop', $($scope.dialog)).height($('.modal-content', $($scope.dialog)).height() + 62);
                            }
                        }, 200);
                    };


                    $scope.colDef = [];
                    $scope.booleanColumn = [];
                    DataService.getTableDesc(refDomainName).then(function (response) {
                        var columns = response.plain();
                        var columnDef = [];
                        $scope.booleanColumn = _.filter(columns, function (n) {
                            return typeof n.booleanValue !== 'undefined';
                        });


                        for (var column in columns) {
                            var tmp = columns[column];
                            columnDef.push({
                                name: tmp.name,
                                index: tmp.name,
                                width: 100,
                                hidden: !tmp.show,
                                sortable: typeof tmp.sortable === 'undefined',
                                label: tmp.title
                            });
                        }

                        $scope.gridConfig = {
                            //data: '',
                            datatype: "custom",
                            cmTemplate: {title: false},
                            height: 200,
                            width: '100%',
                            shrinkToFit: false,
                            colModel: columnDef,
                            multiselect: false,
                            headertitles: true,
                            //toppager:true,
                            viewrecords: true,
                            loadonce: false,
                            pagerpos: 'left',
                            //recordpos: 'center',
                            editurl: "dummy.html",
                            rowNum: $scope.pageSizes[0],
                            rowList: $scope.pageSizes,
                            navConfig: {refresh: false, del: false, add: false, edit: false, search: false},
                            onPaging: function (pgButton) {
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
                                                name: jqgridNameStr,
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
                            jsonReader: {
                                root: 'list',
                                page: 'pageNumber',
                                total: 'pageTotal',
                                records: 'total',
                                repeatitems: false
                            }
                        };
                    });

                    $scope.pageSizes = DEFAULT_PAGE_SIZES;

                    // Refresh the grid, calling the appropriate rest method.
                    $scope.refreshGrid = function () {
                        var reqPage = {
                            pageNumber: $scope.page.pageNumber,
                            pageSize: $scope.page.pageSize,
                            sort: $scope.page.sort
                        };

                        DataService.getPage(refDomainName, $scope.query, reqPage)
                            .then(function (response) {
                                $scope.page = response.plain();
                                if ($scope.page.total % $scope.page.pageSize === 0) {
                                    $scope.page.pageTotal = $scope.page.total / $scope.page.pageSize;
                                } else {
                                    $scope.page.pageTotal = Math.floor($scope.page.total / $scope.page.pageSize) + 1;
                                }
                                if ($scope.booleanColumn && $scope.booleanColumn.length > 0) {
                                    processBooleanValue($scope.page);
                                }
                                $scope.$broadcast('grid.data.update', {
                                    name: jqgridNameStr,
                                    d: $scope.page
                                });
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

                    $scope.$watch('page.pageNumber + "_" + page.pageSize + "_" + page.sort', function () {
                        if ($scope.page.sort != '') {
                            $scope.refreshGrid();
                        }
                    });


                    // ------------------------------------ for select table end ---------------------------


                    $scope.$on('dialog.height.changed', function (event, data) {
                        $(formNameStr).parent().height($(formNameStr).height());
                        $(window).resize();
                    });

                    $scope.$watch(function () {
                        return $(formNameStr).height();
                    }, function (newValue, oldValue) {
                        $(formNameStr).parent().height($(formNameStr).height());
                    });

                    $scope.dependCheck = function () {
                        if ($scope.depend) {
                            if ($scope.depend.indexOf('.') != -1) {
                                var splited = $scope.depend.split('.');
                                var i = 1;
                                var obj = null;
                                if ($scope.entity[splited[0]]) {
                                    obj = $scope.entity[splited[0]];
                                }

                                if (obj) {
                                    for (; i < splited.length; i++) {
                                        if (obj[splited[i]]) {
                                            obj = obj[splited[i]];
                                        } else {
                                            obj = null;
                                            break;
                                        }
                                    }
                                }

                                if (obj) {
                                    $scope.dependVal = {id: obj.id};
                                } else {

                                    DataService.getFormDesc($scope.refDomain.domainName).then(function (response) {
                                        var field = _.find(response.plain(), function (field) {
                                            return field.name === $scope.dependAssociateField;
                                        });
                                        var dialog = DialogService.alert(['请先选择:' + field.title], function () {
                                            dialog.modal('hide');
                                            $scope.$emit('reference.closed');
                                        });
                                    });

                                    return false;
                                }
                            } else {
                                var field = _.find($scope.fields, function (field) {
                                    var fs = field.name.split(".");
                                    return fs[fs.length - 1] === $scope.depend;
                                });
                                if (!field) {
                                    var dialog = DialogService.alert(["依赖关系设置错误！"], function () {
                                        dialog.modal('hide');
                                        $scope.$emit('reference.closed');
                                    });
                                    return false;
                                }
                                var depend = field.name;
                                if ($scope.entity[depend] && $scope.entity[depend].id) {
                                    $scope.dependVal = {id: $scope.entity[depend].id};
                                } else {
                                    var dialog = DialogService.alert(['请先选择:' + field.title], function () {
                                        dialog.modal('hide');
                                        $scope.$emit('reference.closed');
                                    });
                                    return false;
                                }
                            }
                        }
                        return true;
                    };

                    $scope.dialog;

                    $scope.showReference = function (type, domainName) {
                        if ($scope.shouldHide($scope.field.type)) {
                            return;
                        }
                        if (!$scope.dependCheck()) {
                            return;
                        }

                        if ($scope.depend) {
                            $scope.query[$scope.dependAssociateField] = $scope.dependVal;
                        }

                        var refName = type.refName;
                        if ($scope.field.name.indexOf('.') !== -1) {
                            refName = $scope.field.name;
                        }
                        var gridObj = jQuery('#grid-table-' + jqgridNameStr);
                        gridObj.jqGrid('clearGridData');

                        if ($scope.dialog) {
                            $(formNameStr).removeAttr('style');
                            $scope.dialog.modal('show');
                        } else {
                            $scope.dialog = DialogService.dialog({
                                title: '请选择',
                                message: $(formNameStr).removeAttr('style'),
                                //message : formElm.removeAttr('style'),
                                afterClose: function () {
                                    $(formNameStr).attr('style', 'height:0px; display: none');
                                },
                                buttons: {
                                    cancel: {
                                        label: 'Cancel',
                                        className: 'btn-xs',
                                        callback: function () {
                                            $scope.dialog.modal('hide');
                                            $scope.$emit('reference.closed', $scope);
                                        }
                                    },
                                    ok: {
                                        label: '<i class="ace-icon fa fa-check"></i> OK',
                                        className: 'btn-xs btn-success',
                                        callback: function () {
                                        	 $scope.dialog.modal('hide');
                                             $scope.$emit('reference.closed', $scope);
                                            var selr = gridObj.jqGrid('getGridParam', 'selrow');
                                            $scope.entity[refName] = null;
                                            $scope.$apply(function () {
                                                if (typeof selr !== 'undefined' && selr !== null) {
                                                    var rowData = gridObj.jqGrid('getRowData', selr);
                                                    DataService.get(refDomainName, rowData.id).then(function (response) {
                                                        $scope.entity[refName] = response.plain().data;
                                                        $scope.input.val = UtilsService.getValue($scope.entity[refName], $scope.field.type.refLabel);
                                                        $scope.queryClass = 'fa-check green';
                                                       
                                                    });
                                                } else {
//                                                    $scope.dialog.modal('hide');
//                                                    $scope.$emit('reference.closed', $scope);
                                                }
                                            });

                                            // make scroll correct after modal close
                                            //dialog.modal('hide');
                                            //$scope.$emit('reference.closed', $scope);
                                            // make scroll correct after modal close
                                        }
                                    }
                                }
                            });
                        }
                        $('.chosen-select').each(function () {
                            var $this = $(this);
                            $this.next().css({'width': $this.parent().width()});
                        });
                        $scope.$broadcast('dialog.height.changed');
                    };

                    $scope.$on('$destroy', function () {
                        $(formNameStr).remove();
                    });

                    $scope.$on('reference.closed', function (event, data) {
                        if ($scope !== data) {
                            $('body').addClass('modal-open');
                        }
                    });

                },
                link: function (scope, element, attrs) {

                }
            };
        }])

;