/**
 * Created by Jeffrey on 12/5/14.
 */
angular.module('shared.directives.referenceTree', [])

    .directive('referenceTree', ['DataService', 'DialogService', '$compile', 'Session', function (DataService, DialogService, $compile, Session) {
        return {
            restrict: 'A',
            templateUrl: 'app/shared/directives/referenceTree.html',
            replace: true,
            scope: {
                entity: '=',
                field: '=',
                fields: '=',
                domain: '=',
                disabled: '='
            },
            controller: function ($scope) {
                $scope.shouldHide = function (type) {
                    return type.refName === Session.parent.prop || $scope.disabled;
                };

                $scope.searchLabel = '查询';

                $scope.$watch('searchText', function (newValue, oldValue) {
                    //if (newValue === '' || newValue !==) {
                    $scope.searchLabel = '查询';
                    //}
                });

                $scope.showReference = function (type, domainName) {
                    if($scope.shouldHide($scope.field.type)){
                        return;
                    }
                    var pdomainName = null;
                    var refName = type.refName;
                    var refId = type.refId;
                    var refLabel = type.refLabel;
                    var depend = type.depend;
                    var refDomainName = type.refDomainName;
                    var dependAssociateField = type.dependAssociateField;

                    if (refName === Session.parent.prop) {
                        return;
                    }

                    // TODO
                    var multi = false;

                    var plugins = ['types'];

                    var dataUrl = domainName + "/ref/" + refName;

                    if (depend) {
                        if ($scope.entity[depend] && $scope.entity[depend].id) {
                            dataUrl = refDomainName + "/tree" + '/'
                                + dependAssociateField + '/' + $scope.entity[depend].id;
                        } else {
                            var field = _.find($scope.fields, function (field) {
                                return field.name === depend;
                            });
                            var dialog = DialogService.alert(['请先选择:' + field.title], function () {
                                dialog.modal('hide');
                                $scope.$emit('reference.closed');
                            });
                            return;
                        }

                    } else if (refName == 'parent' && Session.parent.name) {
                        dataUrl = domainName + "/tree" + '/'
                            + Session.parent.prop + '/' + Session.parent.id;
                    }

                    var jstreeConfig = {
                        'core': {
                            data: {
                                url: DataService.getBaseUrl() + dataUrl,
                                dataType: "json" // needed only
                                // if you do not
                                // supply JSON
                                // headers
                            }
                        },
                        "types": {
                            "default": {
                                "icon": "assets/img/file.svg"
                                //"icon": "fa fa-file-o"
                            },
                            "demo": {
                                "icon": "fa fa-file-o"
                            }
                        },
                        "plugins": plugins
                    };

                    if (multi) {
                        plugins.push('checkbox');
                        jstreeConfig["checkbox"] = {
                            "keep_selected_style": false
                        };
                    }


                    var outerObj = $('<div />');
                    outerObj.append($('<div class="page-header"></div>').append('<input style="width:50%" class="input-sm" type="text" ng-model="searchText" />&nbsp;&nbsp;'
                        + '<button style="top: -2px" type="button" class="btn btn-primary btn-xs" '
                        + 'ng-click="search()">'
                        + '<i class="fa fa-search"></i>&nbsp;&nbsp;<span ng-bind="searchLabel"></span>'
                        + '</button>'));

                    $compile(outerObj.contents())($scope);

                    $scope.treeObj = $('<div style="height:200px;max-height: 200px;overflow-y: auto;"/>');
                    $scope.treeObj.jstree(jstreeConfig)
                        .bind('click.jstree', function (event) {
                            var eventNodeName = event.target.nodeName;
                            if (eventNodeName === 'A') {
                                $('input', outerObj).val(event.target.text);
                            }
                        });
                    outerObj.append($scope.treeObj);

                    var dialog = DialogService.dialog({
                        title: $scope.field.title,
                        message: outerObj,
                        buttons: {
                            cancel: {
                                "label": 'Cancel',
                                className: 'btn-xs',
                                callback: function () {
                                    $scope.resetSearch();
                                    dialog.modal('hide');
                                    $scope.$emit('reference.closed');
                                }
                            },
                            ok: {
                                label: '<i class="ace-icon fa fa-check"></i> OK',
                                className: 'btn-xs btn-success',
                                callback: function () {
                                    var tree = $.jstree
                                        .reference($scope.treeObj);

                                    var sel = tree.get_selected();
                                    if (sel.length > 0) {
                                        var node = tree
                                            .get_node(sel[0]);
                                        if (typeof $scope.entity[refName] === 'undefined') {
                                            $scope.entity[refName] = {};
                                        }

                                        if ($scope.field.type.refFields.length > 0 || $scope.field.fetch) {
                                            DataService.get(refDomainName, node.id).then(function (response) {
                                                $scope.entity[refName] = response.plain().data;
                                            });
                                        } else {
                                            $scope
                                                .$apply(function () {
                                                    $scope.entity[refName][refLabel] = node.text;
                                                    $scope.entity[refName][refId] = node.id;
                                                });
                                        }
                                    } else {
                                        $scope
                                            .$apply(function () {
                                                delete $scope.entity[refName];
                                            });
                                    }
                                    $scope.resetSearch();
                                    dialog.modal('hide');
                                    $scope.$emit('reference.closed');
                                }
                            }
                        }
                    });
                }
            },
            link: function (scope, element, attrs) {

                scope.searchResult = null;
                scope.searchTextTmp = null;
                scope.searchText = '';

                scope.searchIndex = -1;

                scope.treeData = null;

                scope.resetSearch = function () {
                    scope.searchResult = null;
                    scope.searchTextTmp = null;
                    scope.searchText = '';

                    scope.searchIndex = -1;
                };

                scope.clear = function (type) {
                    if (type.refName === Session.parent.prop) {
                        return;
                    }
                    delete scope.entity[scope.field.type.refName];
                };


                scope.search = function () {
                    //var i = scope.startIndex;
                    //for(; i < scope.plainData.length; i ++){
                    //    scope.startIndex ++;
                    //    if(scope.plainData[i].text.indexOf(scope.searchText)!=-1){
                    //        $.jstree.reference(scope.treeObj).select_node(scope.plainData[i].id);
                    //    }
                    //}

                    if (scope.searchText === '') {
                        return;
                    }

                    if (scope.treeData === null) {
                        scope.treeData = $.jstree.reference(scope.treeObj)._model.data;
                    }

                    if (scope.searchTextTmp !== scope.searchText) {
                        scope.searchIndex = -1;
                        scope.searchTextTmp = scope.searchText;
                    }

                    var take = false;
                    if (scope.searchIndex === -1) {
                        take = true;
                    }

                    var find = false, p;
                    for (p in scope.treeData) {
                        if (scope.treeData.hasOwnProperty(p)) {
                            if (scope.searchIndex === p) {
                                take = true;
                                continue;
                            }
                            if (take && scope.treeData[p].text && scope.treeData[p].text.indexOf(scope.searchText) != -1) {
                                scope.searchIndex = p;
                                var tree = $.jstree.reference(scope.treeObj);
                                tree.deselect_all();
                                tree.select_node(scope.searchIndex, true);
                                find = true;
                                scope.searchLabel = '下一个';
                                var sel = tree.get_selected();

                                if (sel.length > 0) {
                                    var node = tree
                                        .get_node(sel[0], true);

                                    var pos = node.position().top - node.parent().parent().position().top;

                                    $(scope.treeObj).scrollTop($(scope.treeObj).scrollTop() + pos);

                                    //node[0].scrollIntoView();
                                    //node.parentNode.scrollTop = node.offsetTop;
                                }

                                return;
                            }
                        }
                    }

                    if (!find) {
                        for (p in scope.treeData) {
                            if (scope.treeData.hasOwnProperty(p)) {
                                if (scope.treeData[p].text && scope.treeData[p].text.indexOf(scope.searchText) != -1) {
                                    scope.searchIndex = p;
                                    $.jstree.reference(scope.treeObj).deselect_all();
                                    $.jstree.reference(scope.treeObj).select_node(scope.searchIndex, true);
                                    var tree = $.jstree.reference(scope.treeObj);
                                    scope.searchLabel = '下一个';
                                    var sel = tree.get_selected();
                                    if (sel.length > 0) {
                                        var node = tree
                                            .get_node(sel[0], true);
                                        var pos = node.position().top - node.parent().parent().position().top;
                                        $(scope.treeObj).scrollTop($(scope.treeObj).scrollTop() + pos);
                                        //node[0].scrollIntoView();
                                        //node.parentNode.scrollTop = node.offsetTop;
                                    }
                                    return;
                                }
                            }
                        }
                    }

                    scope.searchIndex = -1;

                };


                //scope.$on('$destroy', function () {
                //    $("#reference_tree").jstree("destroy");
                //});
            }
        };
    }])

;