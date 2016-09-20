angular.module('shared.directives.jqGrid', [])
    .directive('ngJqGrid', [function () {
        return {
            restrict: 'A',
            template: '<table id="grid-table-{{name}}"></table><div id="grid-pager-{{name}}"></div>',
            scope: {
                name: '@',
                // data : '=',
                config: '=',
                data: '='
            },
            controller: function ($scope) {

                $scope.$on('$destroy', function (e) {
                    //$scope.tableObj.jqGrid('GridDestroy');
                    $('.ui-jqdialog').remove();
                    $scope.tableObj.remove();
                    $scope.pagerObj.remove();
                });

            },
            link: function (scope, element, attrs) {
                scope.tableObj = $('table', element);
                scope.pagerObj = $('div', element);

                // replace icons with FontAwesome icons like above
                function updatePagerIcons(table) {
                    var replacement = {
                        'ui-icon-seek-first': 'ace-icon fa fa-angle-double-left bigger-140',
                        'ui-icon-seek-prev': 'ace-icon fa fa-angle-left bigger-140',
                        'ui-icon-seek-next': 'ace-icon fa fa-angle-right bigger-140',
                        'ui-icon-seek-end': 'ace-icon fa fa-angle-double-right bigger-140'
                    };
                    $('.ui-pg-table:not(.navtable) > tbody > tr > .ui-pg-button > .ui-icon')
                        .each(
                        function () {
                            var icon = $(this);
                            var $class = $.trim(icon.attr(
                                'class').replace('ui-icon',
                                ''));

                            if ($class in replacement)
                                icon.attr('class', 'ui-icon '
                                    + replacement[$class]);
                        });
                }

                function enableTooltips(table) {
                    $('.navtable .ui-pg-button').tooltip({
                        container: 'body'
                    });
                    $(table).find('.ui-pg-div').tooltip({
                        container: 'body'
                    });
                }

                function style_search_filters(form) {
                    form.find('.delete-rule').val('X');
                    form.find('.add-rule').addClass(
                        'btn btn-xs btn-primary');
                    form.find('.add-group').addClass(
                        'btn btn-xs btn-success');
                    form.find('.delete-group').addClass(
                        'btn btn-xs btn-danger');
                }

                function style_search_form(form) {
                    var dialog = form.closest('.ui-jqdialog');
                    var buttons = dialog.find('.EditTable')
                    buttons.find('.EditButton a[id*="_reset"]').addClass(
                        'btn btn-sm btn-info').find('.ui-icon').attr(
                        'class', 'ace-icon fa fa-retweet');
                    buttons.find('.EditButton a[id*="_query"]').addClass(
                        'btn btn-sm btn-inverse').find('.ui-icon')
                        .attr('class', 'ace-icon fa fa-comment-o');
                    buttons.find('.EditButton a[id*="_search"]').addClass(
                        'btn btn-sm btn-purple').find('.ui-icon').attr(
                        'class', 'ace-icon fa fa-search');
                }

                scope.$watch('config', function (config) {
                    if (typeof config === 'undefined') {
                        return;
                    }

                    //config.url = 'dummy.html';

                    //if(config.datatype === 'json') {
                    //
                    //    config.datatype = "custom";
                    //}
                    config.pager = 'grid-pager-' + scope.name;

                    //config.loadComplete = function () {
                    //    var table = this;
                    //    setTimeout(function () {
                    //        updatePagerIcons(table);
                    //    }, 0);
                    //};

                    var navConfig = config.navConfig;

                    delete config.navConfig;

                    //config.loadComplete = function () {
                    //
                    //    fixPositionsOfFrozenDivs.call(this);
                    //    console.log(11)
                    //};

                    //$(window).on("resize", function () {
                    //    // apply the fix an all grids on the page on resizing of the page
                    //    $("table.ui-jqgrid-btable").each(function () {
                    //        fixPositionsOfFrozenDivs.call(this);
                    //    });
                    //});


                    config.sortable = true;

                    config.altRows = true;
                    config.altclass = 'ui-priority-secondary';

                    scope.gridObj = scope.tableObj.jqGrid(config);

                    jQuery(scope.tableObj).jqGrid('setFrozenColumns');

                    //navButtons
                    jQuery(scope.tableObj).jqGrid('navGrid', '#grid-pager-' + scope.name,
                        navConfig,
                        {},
                        {},
                        {},
                        {
                            //search form
                            recreateForm: true,
                            afterShowSearch: function (e) {
                                var form = $(e[0]);
                                form.closest('.ui-jqdialog').find('.ui-jqdialog-title').wrap('<div class="widget-header" />')
                                style_search_form(form);
                            },
                            afterRedraw: function () {
                                style_search_filters($(this));
                            }
                            ,
                            multipleSearch: true
                            /**
                             multipleGroup:true,
                             showQuery: true
                             */
                        },
                        {
                            //view record form
                            recreateForm: true,
                            beforeShowForm: function (e) {
                                var form = $(e[0]);
                                form.closest('.ui-jqdialog').find('.ui-jqdialog-title').wrap('<div class="widget-header" />')
                            }
                        }
                    );

                    scope.$on('tab.clicked', function (e, d) {
                        $('.tab-pane').each(function (i, d) {
                            if (!$(d).is(':hidden')) {
                                //parent_column.width($(d).width());
                                scope.tableObj.jqGrid('setGridWidth', $(d).width());
                                //$(window).resize();
                            }
                        });
                    });

                    // resize on sidebar collapse/expand
                    var parent_column = scope.tableObj.closest('[class*="col-"]');

                    //alert(parent_column.parent().parent().parent().width());

                    // resize to fit page size
                    $(window).on('resize.' + attrs.gridId, function () {
                        if (parent_column.width() > 100) {
                            scope.tableObj.jqGrid('setGridWidth', parent_column.width());
                        }
                    });

                    scope.$watch(function () {
                        return parent_column.width();
                    }, function (newValue, oldValue) {
                        $(window).resize();
                    }, true);

                    $(document).on(
                        'settings.ace.jqGrid',
                        function (ev, event_name, collapsed) {
                            if (event_name === 'sidebar_collapsed'
                                || event_name === 'main_container_fixed') {
                                // setTimeout is for webkit only to
                                // give time for DOM changes and
                                // then redraw!!!
                                setTimeout(function () {
                                    $(window).triggerHandler(
                                        'resize.' + attrs.gridId);
                                }, 0);
                            }
                        });

                    $(window).triggerHandler('resize.' + attrs.gridId);
                    updatePagerIcons(scope.gridObj);
                });

                scope.$on('grid.row.deleted', function (e, d) {
                    //var rowids = scope.tableObj.jqGrid('getGridParam', 'selarrrow');

                    var rowids = _.clone(d);
                    var i = 0;
                    for (; i < rowids.length; i ++) {
                        scope.tableObj.jqGrid('delRowData', rowids[i]);
                    }
                    scope.tableObj.trigger('reloadGrid');
                });
                scope.$on('grid.data.update', function (e, data) {
                    if (data.name === scope.name) {
                        scope.tableObj.jqGrid('clearGridData');
                        var d = data.d;
                        if (scope.tableObj.jqGrid('getGridParam', 'datatype') === 'custom') {
                            scope.tableObj[0].addJSONData(d);
                        } else {
                            scope.tableObj.jqGrid('setGridParam', {
                                data: d
                            });
                        }
                        scope.tableObj.trigger('reloadGrid');
                        if (typeof scope.tableObj[0].grid !== 'undefined' && scope.tableObj[0].grid != null) {
                            scope.tableObj[0].grid.endReq();
                        }
                        scope.$emit('dialog.height.changed');
                    }
                    $("table.ui-jqgrid-btable").each(function () {
                        fixPositionsOfFrozenDivs.call(this);
                        //resizeColumnHeader.call(this);
                    });
                });

                var resizeColumnHeader = function () {
                        var rowHight, resizeSpanHeight,
                        // get the header row which contains
                            headerRow = $(this).closest("div.ui-jqgrid-view")
                                .find("table.ui-jqgrid-htable>thead>tr.ui-jqgrid-labels");

                        // reset column height
                        headerRow.find("span.ui-jqgrid-resize").each(function () {
                            this.style.height = '';
                        });

                        // increase the height of the resizing span
                        resizeSpanHeight = 'height: ' + headerRow.height() + 'px !important; cursor: col-resize;';
                        headerRow.find("span.ui-jqgrid-resize").each(function () {
                            this.style.cssText = resizeSpanHeight;
                        });

                        // set position of the dive with the column header text to the middle
                        rowHight = headerRow.height();
                        headerRow.find("div.ui-jqgrid-sortable").each(function () {
                            var $div = $(this);
                            $div.css('top', (rowHight - $div.outerHeight()) / 2 + 'px');
                        });
                    },
                    fixPositionsOfFrozenDivs = function () {
                        var $rows;
                        if (this.grid === undefined) {
                            return;
                        }
                        if (this.grid.fbDiv !== undefined) {
                            $rows = $('>div>table.ui-jqgrid-btable>tbody>tr', this.grid.bDiv);
                            $('>table.ui-jqgrid-btable>tbody>tr', this.grid.fbDiv).each(function (i) {
                                var rowHight = $($rows[i]).height(), rowHightFrozen = $(this).height();
                                if ($(this).hasClass("jqgrow")) {
                                    $(this).height(rowHight);
                                    rowHightFrozen = $(this).height();
                                    if (rowHight !== rowHightFrozen) {
                                        $(this).height(rowHight + (rowHight - rowHightFrozen));
                                    }
                                }
                            });
                            $(this.grid.fbDiv).height(this.grid.bDiv.clientHeight + 1);
                            $(this.grid.fbDiv).css($(this.grid.bDiv).position());
                        }
                        if (this.grid.fhDiv !== undefined) {
                            $rows = $('>div>table.ui-jqgrid-htable>thead>tr', this.grid.hDiv);
                            $('>table.ui-jqgrid-htable>thead>tr', this.grid.fhDiv).each(function (i) {
                                var rowHight = $($rows[i]).height(), rowHightFrozen = $(this).height();
                                $(this).height(rowHight);
                                rowHightFrozen = $(this).height();
                                if (rowHight !== rowHightFrozen) {
                                    $(this).height(rowHight + (rowHight - rowHightFrozen));
                                }
                            });
                            $(this.grid.fhDiv).height(this.grid.hDiv.clientHeight);
                            $(this.grid.fhDiv).css($(this.grid.hDiv).position());
                        }
                    },
                    fixGboxHeight = function () {
                        var gviewHeight = $("#gview_" + $.jgrid.jqID(this.id)).outerHeight(),
                            pagerHeight = $(this.p.pager).outerHeight();

                        $("#gbox_" + $.jgrid.jqID(this.id)).height(gviewHeight + pagerHeight);
                        gviewHeight = $("#gview_" + $.jgrid.jqID(this.id)).outerHeight();
                        pagerHeight = $(this.p.pager).outerHeight();
                        $("#gbox_" + $.jgrid.jqID(this.id)).height(gviewHeight + pagerHeight);
                    };
            }
        };
    }]);