/**
 * Created by Jeffrey on 12/11/14.
 */

angular.module('components.selectedSubList', [])
    .constant('DEFAULT_PAGE_SIZES', [20, 50, 100])

    .constant('DEFAULT_PAGE', {
        pageNumber: 1,
        pageSize: 20,
        sort: ''
    })
    .controller('SelectedSubListController', ['$scope',
        'DataService',
        'Session',
        'DialogService',
        'APP_CONFIG',
        'DEFAULT_PAGE_SIZES',
        'DEFAULT_PAGE',
        function ($scope, DataService, Session,
                  DialogService, APP_CONFIG, DEFAULT_PAGE_SIZES, DEFAULT_PAGE) {
            $scope.debug = APP_CONFIG.DEBUG;
            var refDomainName = $scope.field.type.refDomainName,
                refName = $scope.field.name;

            $scope.domain = {
                domainName: refDomainName
            };

            var grid_selector = '#grid-table-' + $scope.domain.domainName;

            $scope.data = [];
            $scope.dataList = [];

            $scope.$watch('dataList', function (newValue, oldValue) {
                $scope.entity[$scope.field.name] = newValue;
            }, true);


            $scope.initData = function (entry) {
                if (typeof entry.id !== 'undefined') {
                    DataService.getSubList(Session.domainName, entry.id, $scope.field.name).then(function (response) {
                        $scope.dataList = response.plain();
                        $scope.data = [];
                        var i;

                        for (i = 0; i < $scope.dataList.length; i++) {
                            $scope.data.push($scope.transformData($scope.dataList[i], $scope.columnDef));
                        }

                        $scope.entity[$scope.field.name] = $scope.dataList;
                        $scope.$broadcast('grid.data.update', {name: $scope.field.name, d: $scope.data});
                        setTimeout(function () {
                            $scope.$emit('sub.init.finished', $scope.field.name);
                        }, 0);
                    });
                } else {
                    setTimeout(function () {
                        $scope.$emit('sub.init.finished', $scope.field.name);
                    }, 0);
                }
            };

            $scope.$on('domain.saved', function (event, data) {
                $scope.initData(data);
            });

            $scope.$on('dialog.height.changed', function (event, data) {
                $('#sub-' + $scope.field.name).parent().height($('#sub-' + $scope.field.name).height());
                $(window).resize();
            });

            $scope.$on('reference.closed', function (event, data) {
                $('body').addClass('modal-open');
            });

            $scope.$watch(function () {
                return $('#sub-' + $scope.field.name).height();
            }, function (newValue, oldValue) {
                $('#sub-' + $scope.field.name).parent().height($('#sub-' + $scope.field.name).height());
            });

            $scope.sub = {};

            DataService.getFormDesc(refDomainName).then(function (response) {
                $scope.subFields = response.plain();

                DataService.getTableDesc($scope.domain.domainName).then(function (response) {
                    var columns = response.plain();
                    var columnDef = [];


                    for (var column in columns) {
                        var tmp = columns[column];
                        columnDef.push({
                            name: tmp.name,
                            index: tmp.name,
                            width: 100,
                            hidden: !tmp.show,
                            label: tmp.title
                        });
                    }

                    $scope.columnDef = columnDef;

                    //DataService.getTableDesc(refDomainName).then(function (response) {
                    $scope.config = {
                        datatype: "local",
                        height: 250,
                        //shrinkToFit: false,
                        //colNames: colNames,
                        colModel: columnDef,

                        viewrecords: true,
                        rowNum: 10,
                        rowList: [10, 20, 30],

                        altRows: true,
                        // toppager: true,

                        multiselect: true,
                        // multikey: "ctrlKey",
                        //multiboxonly: true,
                        //,
                        //
                        //
                        //
                        editurl: "dummy.html"
                        //,// nothing is saved
                        //caption: "jqGrid with inline editing"

                        // ,autowidth: true,

                        /**
                         * , grouping:true, groupingView : { groupField : ['name'],
					 * groupDataSorted : true, plusicon : 'fa fa-chevron-down
					 * bigger-110', minusicon : 'fa fa-chevron-up bigger-110' },
                         * caption: "Grouping"
                         */
                    };
                    if (($scope.readonly + '') === 'true' || $scope.field.type.disabled) {
                        $scope.config = angular.extend({
                            pagerpos: 'left',
                            navConfig: {refresh: false, del: false, add: false, edit: false, search: false},
                            multiselect: false,
                            //multiselectWidth: 0,
                            hoverrows: false,
                            gridComplete: function () {
                                $(this).jqGrid('hideCol', 'cb');
                            }
                        }, $scope.config);

                    } else {
                        $scope.config = angular.extend({
                            navConfig: { 	//navbar options
                                edit: false,
                                editicon: 'ace-icon fa fa-pencil blue',
                                add: true,
                                addicon: 'ace-icon fa fa-plus-circle purple',
                                del: true,
                                delicon: 'ace-icon fa fa-trash-o red',
                                search: true,
                                searchicon: 'ace-icon fa fa-search orange',
                                refresh: true,
                                refreshicon: 'ace-icon fa fa-refresh green',
                                view: true,
                                viewicon: 'ace-icon fa fa-search-plus grey',
                                delfunc: function (rowid) {
                                    var self = this;

                                    DialogService.confirm(['您确定要删除这些纪录吗？'], function () {
                                        //$scope.$apply(function () {
                                        var i = 0;
                                        for (; i < rowid.length; i++) {
                                            var gridRow = $(self).jqGrid('getRowData', rowid[i]);
                                            $scope.dataList.splice(_.findIndex($scope.dataList, function (chr) {
                                                return chr.id == gridRow.id;
                                            }), 1);


                                            $scope.data.splice(_.findIndex($scope.data, function (chr) {
                                                return chr.id == gridRow.id;
                                            }), 1);
                                        }
                                        //});
                                        $scope.$broadcast('grid.row.deleted', rowid);
                                    });
                                },
                                addfunc: function () {
                                    //$scope.$apply(function () {
                                    //    $scope.sub = {};
                                    //    var i;
                                    //    for (i = 0; i < $scope.subFields.length; i++) {
                                    //        var f = $scope.subFields[i];
                                    //        if (typeof f.defaultVal !== 'undefined') {
                                    //            $scope.sub[f.name] = f.defaultVal;
                                    //        }
                                    //    }
                                    //    //$scope.$broadcast('sub.add.start');
                                    //});

                                    var gridObj = jQuery(grid_selector);
                                    gridObj.jqGrid('clearGridData');
                                    //$scope.$apply(function () {
                                    //    $scope.$broadcast('grid.data.update', {name: $scope.field.name, d: []});
                                    //});

                                    $scope.dialog = DialogService.dialog({
                                        title: '添加',
                                        message: $('#sub-' + $scope.field.name).removeAttr('style'),
                                        //message : formElm.removeAttr('style'),
                                        afterClose: function () {
                                            $('#sub-' + $scope.field.name).attr('style', 'height:0px; overflow: hidden');
                                        },
                                        buttons: {
                                            cancel: {
                                                label: 'Cancel',
                                                className: 'btn-xs',
                                                callback: function () {

                                                }
                                            },
                                            ok: {
                                                label: '<i class="ace-icon fa fa-check"></i> OK',
                                                className: 'btn-xs btn-success',
                                                callback: function () {
                                                    var selr = gridObj.jqGrid('getGridParam', 'selarrrow');
                                                    $scope.$apply(function () {
                                                        var i = 0;
                                                        for (; i < selr.length; i++) {
                                                            var rowData = gridObj.jqGrid('getRowData', selr[i]);
                                                            if (_.findIndex($scope.dataList, function (chr) {
                                                                    return chr.id == selr[i];
                                                                }) == -1) {
                                                                $scope.dataList.push({id: rowData.id});
                                                                $scope.data.push(rowData);
                                                            }
                                                        }
                                                        $scope.$broadcast('grid.data.update', {
                                                            name: $scope.field.name,
                                                            d: $scope.data
                                                        });
                                                    });
                                                }
                                            }
                                        }
                                    });
                                    $(window).trigger('resize.chosen');
                                    $scope.$broadcast('dialog.height.changed');
                                }
                            }
                        }, $scope.config);
                    }

                    $scope.initData($scope.entity);
                });
            });

            $scope.transformData = function (item, columnDef) {
                var dataItem = {},
                    j;
                for (j = 0; j < columnDef.length; j++) {
                    var field = _.find($scope.subFields, function (field) {
                        return field.name === columnDef[j].name;
                    });
                    if (columnDef[j].name.indexOf('_') !== -1) {
                        var splited = columnDef[j].name.split('_');
                        if (item[splited[0]]) {
                            dataItem[columnDef[j].name] = item[splited[0]][splited[1]];
                        }
                    } else if (field) {
                        if (field.type.view === 'boolean') {
                            var tVal = _.find(field.type.options, function (field) {
                                return field.id == item[columnDef[j].name];
                            });
                            if(tVal){
                            	dataItem[columnDef[j].name] = tVal.name;
                            }
                        } else if (field.type.view === 'radio') {
                            var tVal = _.find(field.type.options, function (field) {
                                return field.id == item[columnDef[j].name].id;
                            });
                            if(tVal){
                            	dataItem[columnDef[j].name] = tVal.name;
                            }
                        } else {
                            dataItem[columnDef[j].name] = item[columnDef[j].name];
                        }
                    }
                }
                return dataItem;
            };

// var selr =
// jQuery(grid_selector).jqGrid('getGridParam','selrow');

            $scope.$on('$destroy', function (e) {
                $('#sub-' + $scope.field.name).remove();
            });


            $scope.query = {};

            $scope.page = DEFAULT_PAGE;

            $scope.queryAreaToggle = {
                hideButtonName: '展开',
                showButtonName: '收起',
                style: {overflow: 'hidden', height: '0px'},
                isShown: false
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

            $scope.colDef = [];
            DataService.getTableDesc($scope.domain.domainName).then(function (response) {
                var columns = response.plain();
                var columnDef = [];


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
                    multiselect: true,
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

                DataService.getPage($scope.domain.domainName, $scope.query, reqPage)
                    .then(function (response) {
                        $scope.page = response.plain();
                        if ($scope.page.total % $scope.page.pageSize === 0) {
                            $scope.page.pageTotal = $scope.page.total / $scope.page.pageSize;
                        } else {
                            $scope.page.pageTotal = Math.floor($scope.page.total / $scope.page.pageSize) + 1;
                        }
                        $scope.$broadcast('grid.data.update', {name: $scope.domain.domainName, d: $scope.page});
                    });
            };

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
                $scope.refreshGrid();
            });
        }])
;
