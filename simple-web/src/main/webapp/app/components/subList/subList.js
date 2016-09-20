/**
 * Created by Jeffrey on 12/11/14.
 */

angular.module('components.subList', [])

    .controller('SubEditController', ['$scope', function ($scope) {

    }])

    .controller('SubListController', ['$scope',
        'DataService',
        'Session',
        'DialogService',
        'APP_CONFIG',
        function ($scope, DataService, Session,
                  DialogService, APP_CONFIG) {
            $scope.debug = APP_CONFIG.DEBUG;
            var refDomainName = $scope.field.type.refDomainName,
                associateFieldName = $scope.field.type.dependAssociateField,
                refName = $scope.field.name;

            $scope.domain = {
                domainName: refDomainName
            };

            $scope.data = [];
            $scope.dataList = [];

            $scope.$watch('dataList', function (newValue, oldValue) {
                $scope.entity[$scope.field.name] = newValue;
            }, true);

            //$scope.$watch('entity.id', function (newValue, oldValue) {
            //    if (typeof newValue !== 'undefined') {
            //        DataService.getSubList(Session.domainName, newValue, $scope.field.name).then(function (response) {
            //            $scope.dataList = response.plain();
            //            $scope.data = [];
            //            var i;
            //
            //            for (i = 0; i < $scope.dataList.length; i++) {
            //                $scope.data.push($scope.transformData($scope.dataList[i], $scope.columnDef));
            //            }
            //
            //            //$scope.entity[$scope.field.name] = $scope.dataList;
            //            $scope.$broadcast('grid.data.update', {name: $scope.field.name, d: $scope.data});
            //            //setTimeout(function () {
            //            //    $scope.$emit('sub.init.finished', $scope.field.name);
            //            //}, 0);
            //        });
            //    }
            //}, true);


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
                            if($scope.inDailog){
                            	$('body').addClass('modal-open');
                            }
                        }, 0);
                    });
                } else {
                    setTimeout(function () {
                        $scope.$emit('sub.init.finished', $scope.field.name);
                    }, 0);
                }
            };
            $scope.inDailog=false;
            $scope.$on('sub.show.dailog', function (event) {
            	$scope.inDailog=true;
            });
            
            $scope.$on('domain.saved', function (event, data) {
                $scope.initData(data);
            });

            //var contentObj;
            //
            //var getContentObj = function () {
            //    if(typeof contentObj === 'undefined'){
            //        contentObj = $('#sub-' + $scope.field.name);
            //    }
            //    return contentObj;
            //};

            $scope.$on('dialog.height.changed', function (event, data) {
                $('#sub-' + $scope.field.name).parent().height($('#sub-' + $scope.field.name).height());
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

            $scope.defaultSub = {};

            $scope.parentFieldName;

            $scope.$on('object.dependency.found', function (e, d) {
                if (d.split('.')[0] === $scope.parentFieldName) {
                    var watchField = d.replace($scope.parentFieldName + ".", 'entity.');
                    $scope.$watch(watchField + '.id', function (n, o) {
                        if ((n || o) && n != o ) {
                        	$scope.defaultSub[$scope.parentFieldName] = $scope.entity;
                            $scope.data = [];
                            $scope.dataList = [];
                            $scope.$broadcast('grid.data.update', {
                                name: $scope.field.name,
                                d: $scope.data
                            });
                        }
                    });
                }
            });

            DataService.getFormDesc(refDomainName).then(function (response) {
                var rtn = response.plain();
                $scope.subFields = [];

                for (i = 0; i < rtn.length; i++) {
                    var field = rtn[i];
                    if (field.type.view === 'reference'
                        && field.type.refDomainName === Session.domainName) {
                        $scope.parentFieldName = field.name;
                        $scope.defaultSub[$scope.parentFieldName] = $scope.entity;
                        continue;
                    }

                    $scope.subFields.push(field);
                }


                var columnDef = [],
                    colNames = [],
                    i;

                colNames.push('tmpId');
                columnDef.push({
                    name: 'tmpId',
                    index: 'tmpId',
                    hidden: true
                });


                for (i = 0; i < $scope.subFields.length; i++) {
                    var field = $scope.subFields[i];

                    if (typeof field.defaultVal !== 'undefined') {
                        $scope.defaultSub[field.name] = field.defaultVal;
                    }

                    //$scope.sub = $scope.defaultSub;


                    if (field.visable && (field.type.view === 'reference'
                        || field.type.view === 'select')) {
                        colNames.push('');
                        columnDef.push({
                            name: field.name + '_' + field.type.refId,
                            index: field.name + '_' + field.type.refId,
                            hidden: true
                        });
                        columnDef.push({
                            width: 150,
                            name: field.name + '_' + field.type.refLabel.split('.').join('_'),
                            index: field.name + '_' + field.type.refLabel.split('.').join('_')
                        });
                    } else if (field.type.view === 'hidden') {
                        columnDef.push({
                            name: field.name,
                            index: field.name,
                            hidden: true
                        });
                    } else {
                        columnDef.push({
                            width: 150,
                            name: field.name,
                            index: field.name
                        });
                    }

                    //if (field.type.view !== 'hidden') {
                    colNames.push(field.title);
                    //}

                    if (typeof field.type.depend !== 'undefined' && field.type.depend.indexOf('.') === -1) {
                        (function (field) {
                            var fieldName = field.name;
                            var depend = field.type.depend;
                            $scope.$watch('sub.' + field.type.depend, function (newValue, oldValue) {
                                if (typeof oldValue === 'undefined') {
                                    return;
                                }
                                delete $scope.sub[fieldName][depend];
                            }, true);
                        })(field);
                    }

                    if (field.hasChangedListener) {
                        (function (field) {
                            var watchKey;
                            if (field.type.refId === undefined) {
                                watchKey = 'sub.' + field.name;
                            } else {
                                watchKey = 'sub.' + field.name + "." + field.type.refId;
                            }

                            var inst=null;
                            $scope.$watch(watchKey, function (newValue, oldValue) {
                                if(inst!=null){
                                	clearTimeout(inst);
                                }
                                if (newValue == oldValue) {
                                    return;
                                }
                                inst=setTimeout(function(){
                            		
                            		 var cache = {};

                                     for (var p in $scope.sub) {
                                         if ($scope.sub.hasOwnProperty(p) && _.isArray($scope.sub[p])) {
                                             cache[p] = $scope.sub[p];
                                         }
                                     }

                                     DataService.changed($scope.domain.domainName, field.name, $scope.sub)
                                         .then(function (response) {
                                             var rtn = response.plain();
                                             if (rtn.status.code === 200) {
                                                 //scope.$apply(function () {
                                                 $scope.sub = response.plain().data;
                                                 for (var p in cache) {
                                                     if (cache.hasOwnProperty(p)) {
                                                         $scope.sub[p] = cache[p];
                                                     }
                                                 }
                                                 //});
                                             }
                                         });
                            	},1000)
                               
                            });
                        }(field));
                    }

                    if (typeof field.refField !== 'undefined') {
                        (function (field) {
                            var index = _.findIndex($scope.subFields, function (chr) {
                                return chr.name === field.refField;
                            });

                            if (index > -1) {
                                $scope.subFields[index].fetch = true;
                            }
                            $scope.$watch('sub.' + field.refField, function (newValue, oldValue) {
                                if (typeof newValue !== 'undefined' && newValue != null && typeof oldValue !== 'undefined' && !_.isEqual(newValue, oldValue)) {

                                    var splited = field.refPath.split('.');
                                    var i = 1;
                                    var obj = null;
                                    if (newValue[splited[0]]) {
                                        obj = newValue[splited[0]];
                                    }

                                    if (obj) {
                                        for (; i < splited.length; i++) {
                                            if (obj[splited[i]]) {
                                                obj = obj[splited[i]];
                                            } else {
                                                break;
                                            }
                                        }
                                    }

                                    if (obj) {
                                        $scope.sub[field.name] = obj;
                                    }
                                } else if (typeof newValue === 'undefined' && typeof oldValue !== 'undefined') {
                                    delete $scope.sub[field.name];
                                }
                            }, true);
                        }(field));
                    }


                    if (field.type.refFields && field.type.refFields.length > 0) {
                        var j = 0;

                        for (; j < field.type.refFields.length; j++) {
                            var refField = field.type.refFields[j];
                            columnDef.push({
                                width: 150,
                                name: field.name + '_' + refField.name.split('.').join('_'),
                                index: field.name + '_' + refField.name.split('.').join('_')
                            });
                            colNames.push(refField.title);
                        }
                    }
                }

                $scope.columnDef = columnDef;

                //DataService.getTableDesc(refDomainName).then(function (response) {
                $scope.config = {
                    datatype: "local",
                    height: 250,
                    //shrinkToFit: false,
                    colNames: colNames,
                    colModel: columnDef,

                    viewrecords: true,
                    rowNum: 10,
                    rowList: [10, 20, 30],

                    altRows: true,
                    // toppager: true,

                    multiselect: true,
                    // multikey: "ctrlKey",
                    multiboxonly: true,
                    shrinkToFit: false,
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

                $scope.dialog;
                $scope.action;
                $scope.editRowIndex;
                $scope.createSubEditDialog = function () {
                    if ($scope.dialog) {
                        $('#sub-' + $scope.field.name).removeAttr('style');
                        $scope.dialog.modal('show');
                    } else {
                        $scope.dialog = DialogService.dialog({
                            title: '编辑',
                            message: $('#sub-' + $scope.field.name).removeAttr('style'),
                            afterClose: function () {
                                $('#sub-' + $scope.field.name).attr('style', 'height:0px; min-height:0px; overflow: hidden');
                                delete $scope.validation;
                            },
                            buttons: {
                                cancel: {
                                    label: '<i class="ace-icon fa fa-close"></i> 取消',
                                    className: 'btn-xs'
                                },
                                ok: {
                                    label: '<i class="ace-icon fa fa-check"></i> <span>保存</span>',
                                    className: 'btn-xs btn-success',
                                    callback: function (e) {
                                        var btn = $(e.target).closest('button');

                                        $('span', btn).text('处理中');
                                        $('i', btn).removeClass('fa-check').addClass('fa-spinner');
                                        btn.attr('disabled', 'disabled');


                                    	if (associateFieldName && $scope.entity.id) {
                                          $scope.sub.associateFieldName = {id: $scope.entity.id};
                                    	}
                                        if ($scope.action === 'add') {
                                            DataService.validate($scope.field.type.refDomainName, $scope.sub).then(
                                                function (response) {
                                                    var rtnData = response.plain();
                                                    if (rtnData.status.code === 200) {
                                                        $scope.sub.tmpId = new Date().getTime();
                                                        $scope.sub[associateFieldName] = $scope.sub.associateFieldName;
                                                        $scope.dataList.push($scope.sub);
                                                        $scope.data.push($scope.transformData($scope.sub, columnDef));
                                                        $scope.$broadcast('grid.data.update', {
                                                            name: $scope.field.name,
                                                            d: $scope.data
                                                        });
                                                        $scope.dialog.modal('hide');
                                                    } else {
                                                        $scope.validation = rtnData.infos.join('\n');
                                                        $scope.$broadcast('dialog.height.changed');
                                                    }
                                                    if (associateFieldName) {
                                                        delete $scope.sub.associateFieldName;
                                                    }

                                                    $('span', btn).text('保存');
                                                    $('i', btn).removeClass('fa-spinner').addClass('fa-check');
                                                    btn.removeAttr('disabled');
                                                }
                                            );
                                        } else if ($scope.action === 'edit') {
                                            DataService.validate($scope.field.type.refDomainName, $scope.sub).then(
                                                function (response) {
                                                    var rtnData = response.plain();
                                                    if (rtnData.status.code === 200) {
                                                    	$scope.sub[associateFieldName] = $scope.sub.associateFieldName;
                                                        $scope.dataList[$scope.editRowIndex] = $scope.sub;
                                                        $scope.data[$scope.editRowIndex] = $scope.transformData($scope.sub, columnDef);
                                                        //$scope.sub = _.cloneDeep($scope.defaultSub);
                                                        $scope.$broadcast('grid.data.update', {
                                                            name: $scope.field.name,
                                                            d: $scope.data
                                                        });
                                                        $scope.dialog.modal('hide');
                                                    } else {
                                                        $scope.validation = rtnData.infos.join('\n');
                                                        $scope.$broadcast('dialog.height.changed');
                                                    }
                                                    if (associateFieldName) {
                                                        delete $scope.sub.associateFieldName;
                                                    }

                                                    $('span', btn).text('保存');
                                                    $('i', btn).removeClass('fa-spinner').addClass('fa-check');
                                                    btn.removeAttr('disabled');
                                                }
                                            );
                                        }

                                        return false;
                                    }
                                }
                            }
                        });
                    }
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
                            edit: true,
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
                                    var i = 0;
                                    for (; i < rowid.length; i++) {
                                        var gridRow = $(self).jqGrid('getRowData', rowid[i]);

                                        $scope.dataList.splice(_.findIndex($scope.dataList, function (chr) {
                                            return chr.id == gridRow.id || chr.tmpId == gridRow.tmpId;
                                        }), 1);


                                        $scope.data.splice(_.findIndex($scope.data, function (chr) {
                                            return chr.id == gridRow.id || chr.tmpId == gridRow.tmpId;
                                        }), 1);
                                    }

                                    $scope.$broadcast('grid.data.update', {
                                        name: $scope.field.name,
                                        d: $scope.data
                                    });
                                    //$scope.$broadcast('grid.row.deleted', rowid);
                                });
                            },
                            addfunc: function () {
                                $scope.action = 'add';

                                $scope.$apply(function () {
                                    $scope.sub = _.cloneDeep($scope.defaultSub);
                                    $scope.$broadcast('editable.reference.changed', $scope.sub);
                                    $scope.createSubEditDialog();
                                    $(window).trigger('resize.chosen');
                                    $scope.$broadcast('dialog.height.changed');
                                });
                            },
                            editfunc: function (rowid) {
                                var gridRow = $(this).jqGrid('getRowData', rowid);

                                var index = _.findIndex($scope.dataList, function (chr) {
                                    return chr.id == gridRow.id || chr.tmpId == gridRow.tmpId
                                });

                                $scope.editRowIndex = index;
                                $scope.action = 'edit';

                                $scope.$apply(function () {
                                    $scope.sub = _.clone($scope.dataList[index], true);
                                    if($scope.parentFieldName){
                                        $scope.sub[$scope.parentFieldName] =$scope.entity;
                                    }
                                    $scope.$broadcast('editable.reference.changed', $scope.sub);
                                    //$scope.$broadcast('datetime.changed');

                                    $scope.createSubEditDialog();

                                    $(window).trigger('resize.chosen');
                                    $('#sub-' + $scope.field.name).parent().height($('#sub-' + $scope.field.name).height());
                                });
                            }
                        }
                    }, $scope.config);
                }

                $scope.initData($scope.entity);
            });
            //});

            $scope.transformData = function (item, columnDef) {
                var dataItem = {},
                    j;
                for (j = 0; j < columnDef.length; j++) {
                    if (columnDef[j].name === $scope.parentFieldName) {
                        continue;
                    }
                    var field = _.find($scope.subFields, function (field) {
                        return field.name === columnDef[j].name;
                    });
                    if (columnDef[j].name.indexOf('_') !== -1) {
                        var splited = columnDef[j].name.split('_');
                        var i = 1;
                        var obj = null;
                        if (item[splited[0]]) {
                            obj = item[splited[0]];
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
                            dataItem[columnDef[j].name] = obj;
                        }
                    } else if (field) {
                        if (field.type.view === 'boolean') {
                            var tVal = _.find(field.type.options, function (field) {
                                return field.id + '' === item[columnDef[j].name] + '';
                            });
                            if (tVal) {
                                dataItem[columnDef[j].name] = tVal.name;
                            }
                        } else if (field.type.view === 'radio') {
                            var tVal = _.find(field.type.options, function (field) {
                                return item[columnDef[j].name] && (field.id + '' == item[columnDef[j].name].id + '');
                            });
                            if (tVal) {
                                dataItem[columnDef[j].name] = tVal.name;
                            }
                        } else {
                            dataItem[columnDef[j].name] = item[columnDef[j].name];
                        }
                    }

                    if (typeof item.tmpId !== 'undefined') {
                        dataItem.tmpId = item.tmpId;
                    }
                }
                return dataItem;
            };

            var grid_selector = "#grid-table";
            var pager_selector = "#grid-pager";

// switch element when editing inline
            function aceSwitch(cellvalue, options, cell) {
                setTimeout(function () {
                    $(cell).find('input[type=checkbox]').addClass(
                        'ace ace-switch ace-switch-5').after(
                        '<span class="lbl"></span>');
                }, 0);
            }

// enable datepicker
            function pickDate(cellvalue, options, cell) {
                setTimeout(function () {
                    $(cell).find('input[type=text]').datepicker({
                        format: 'yyyy-mm-dd',
                        autoclose: true
                    });
                }, 0);
            }

            function style_edit_form(form) {
                // enable datepicker on "sdate" field and switches for
                // "stock" field
                form.find('input[name=sdate]').datepicker({
                    format: 'yyyy-mm-dd',
                    autoclose: true
                }).end().find('input[name=stock]').addClass(
                    'ace ace-switch ace-switch-5').after(
                    '<span class="lbl"></span>');
                // don't wrap inside a label element, the checkbox value
                // won't be submitted (POST'ed)
                // .addClass('ace ace-switch ace-switch-5').wrap('<label
                // class="inline" />').after('<span
                // class="lbl"></span>');

                // update buttons classes
                var buttons = form.next()
                    .find('.EditButton .fm-button');
                buttons.addClass('btn btn-sm').find('[class*="-icon"]')
                    .hide();// ui-icon, s-icon
                buttons.eq(0).addClass('btn-primary').prepend(
                    '<i class="ace-icon fa fa-check"></i>');
                buttons.eq(1).prepend(
                    '<i class="ace-icon fa fa-times"></i>');

                buttons = form.next().find('.navButton a');
                buttons.find('.ui-icon').hide();
                buttons.eq(0).append(
                    '<i class="ace-icon fa fa-chevron-left"></i>');
                buttons.eq(1).append(
                    '<i class="ace-icon fa fa-chevron-right"></i>');
            }

            function style_delete_form(form) {
                var buttons = form.next()
                    .find('.EditButton .fm-button');
                buttons.addClass('btn btn-sm btn-white btn-round')
                    .find('[class*="-icon"]').hide();// ui-icon,
                // s-icon
                buttons.eq(0).addClass('btn-danger').prepend(
                    '<i class="ace-icon fa fa-trash-o"></i>');
                buttons.eq(1).addClass('btn-default').prepend(
                    '<i class="ace-icon fa fa-times"></i>')
            }

            function beforeDeleteCallback(e) {
                var form = $(e[0]);
                if (form.data('styled'))
                    return false;

                form.closest('.ui-jqdialog').find(
                    '.ui-jqdialog-titlebar').wrapInner(
                    '<div class="widget-header" />');
                style_delete_form(form);

                form.data('styled', true);
            }

            function beforeEditCallback(e) {
                var form = $(e[0]);
                form.closest('.ui-jqdialog').find(
                    '.ui-jqdialog-titlebar').wrapInner(
                    '<div class="widget-header" />');
                style_edit_form(form);
            }

// var selr =
// jQuery(grid_selector).jqGrid('getGridParam','selrow');

            $scope.$on('$destroy', function (e) {
                $(grid_selector).jqGrid('GridUnload');
                $('.ui-jqdialog').remove();
                $('#sub-' + $scope.field.name).remove();
            });

        }])
;
