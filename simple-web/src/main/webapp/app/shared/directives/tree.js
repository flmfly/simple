/**
 * Created by Jeffrey on 12/5/14.
 */
angular.module('shared.directives.tree', [])

    .directive('tree', ['$compile', function ($compile) {
        return {
            restrict: 'A',
            //templateUrl: 'app/shared/directives/tree.html',
            replace: true,
            scope: {},
            controller: function ($scope) {
                $scope.treeObj = $('<div/>');

                $scope.jstreeConfig = {
                    core: {
                        data: {
                            //url: DataService.getBaseUrl() + scope.domain.domainName + "/tree",
                            dataType: "json" // needed only
                            // if you do not
                            // supply JSON
                            // headers
                        }
                    },
                    "types": {
                        "default": {
                            "icon": "fa fa-file-o"
                        },
                        "demo": {
                            "icon": "fa fa-folder-o"
                        }
                    },
                    "plugins": ['types']
                };

                $scope.buildTree = function (data) {
                    if (data.url !== $scope.jstreeConfig.core.data.url) {
                        $($scope.treeObj).jstree("destroy");
                    }

                    $scope.jstreeConfig.core.data.url = data.url;

                    $scope.treeObj.jstree($scope.jstreeConfig);
                    $scope.treeObj.unbind("select_node.jstree")
                        .bind('select_node.jstree', function (event) {
                            var sel = $.jstree.reference($scope.treeObj).get_selected();
                            if (sel.length > 0) {
                                $scope.$emit('node.click', sel[0]);
                            }
                        });

                };

                $scope.searchResult = null;
                $scope.searchTextTmp = null;
                $scope.searchText = '';

                $scope.searchIndex = -1;

                $scope.treeData = null;

                $scope.resetSearch = function () {
                    $scope.searchResult = null;
                    $scope.searchTextTmp = null;
                    $scope.searchText = '';

                    $scope.searchIndex = -1;
                };


                $scope.search = function () {
                    if ($scope.treeData === null) {
                        $scope.treeData = $.jstree.reference($scope.treeObj)._model.data;
                    }

                    if ($scope.searchTextTmp !== $scope.searchText) {
                        $scope.searchIndex = -1;
                        $scope.searchTextTmp = $scope.searchText;
                    }

                    var take = false;
                    if ($scope.searchIndex === -1) {
                        take = true;
                    }

                    var find = false, p;
                    for (p in $scope.treeData) {
                        if ($scope.treeData.hasOwnProperty(p)) {
                            if ($scope.searchIndex === p) {
                                take = true;
                                continue;
                            }
                            if (take && $scope.treeData[p].text && $scope.treeData[p].text.indexOf($scope.searchText) != -1) {
                                $scope.searchIndex = p;
                                $.jstree.reference($scope.treeObj).deselect_all();
                                $.jstree.reference($scope.treeObj).select_node($scope.searchIndex, true);
                                find = true;
                                return;
                            }
                        }
                    }

                    if (!find) {
                        for (p in $scope.treeData) {
                            if ($scope.treeData.hasOwnProperty(p)) {
                                if ($scope.treeData[p].text && $scope.treeData[p].text.indexOf($scope.searchText) != -1) {
                                    $scope.searchIndex = p;
                                    $.jstree.reference($scope.treeObj).deselect_all();
                                    $.jstree.reference($scope.treeObj).select_node($scope.searchIndex, true);
                                    return;
                                }
                            }
                        }
                    }

                    $scope.searchIndex = -1;
                };

                $scope.$on('$destroy', function () {
                    try {
                        //$($scope.treeObj).jstree("destroy");
                        $($scope.treeObj).remove();
                    }catch(e){

                    }
                });

                $scope.$on('tree.refresh', function (event, data) {
                    $scope.buildTree(data);

                    var ref = $.jstree.reference($scope.treeObj);
                    $scope.treeObj.unbind("refresh.jstree").bind(
                        "refresh.jstree", function () {
                            if (data.single) {
                                ref.deselect_all();
                            } else {
                                if (ref.save_selected) {
                                    ref.save_selected();
                                }
                            }

                            if (typeof data.node.id !== 'undefined') {
                                var selectedId = data.node.id;
                                if (typeof ref._model.data[selectedId] === 'undefined'
                                    && typeof data.node.parent !== 'undefined') {
                                    selectedId = data.node.parent.id;
                                }
                                ref.select_node(selectedId, true);
                            }
                        });
                    ref.refresh();
                });

            },
            link: function (scope, element, attrs) {
                var outerObj = $('<div />');
                outerObj.append($('<div class="page-header"></div>').append('<input class="input-sm" type="text" ng-model="searchText" />&nbsp;&nbsp;'
                + '<div class="btn-group"><button style="top: -1px" type="button" class="btn btn-primary btn-xs" '
                + 'ng-click="search()">'
                + '<i class="fa fa-search"></i>&nbsp;&nbsp;查询'
                + '</button></div>'));

                $compile(outerObj.contents())(scope);

                outerObj.append(scope.treeObj);
                element.replaceWith(outerObj.children());

                scope.$emit('tree.directive.loaded');
            }
        }

    }])

;