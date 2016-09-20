/**
 * Created by Jeffrey on 12/5/14.
 */
angular.module('shared.directives.referenceMap', [])

    .directive('referenceMap', ['DataService', 'DialogService',
        function (DataService, DialogService) {
            var DEFAULT_PAGE = {
                pageNumber: 1,
                pageSize: -1,
                sort: ''
            };
            return {
                restrict: 'A',
                templateUrl: 'app/shared/directives/referenceMap.tpl.html',
                replace: true,
                scope: {
                    entity: '=',
                    field: '='
                    //,
                    //address: '='
                },
                controller: function ($scope, $compile) {
                    $scope.refDomain = {
                        domainName: $scope.field.type.refDomainName
                    };
                    $scope.query = {};
                    $scope.page = DEFAULT_PAGE;
                    var refDomainName = $scope.field.type.refDomainName;
                    $scope.canSearch = false;

                    $scope.address = null;

                    if (typeof $scope.field.mapAddress !== 'undefined') {
                        $scope.$watch('address', function (newValue) {
                            if (newValue !== null) {
                                $scope.entity[$scope.field.mapAddress] = newValue;
                            }
                        });
                    }

                    $scope.mapInfoProperties = [];
                    $scope.mapInfoTitleProperty;
                    $scope.longitudeProperty;
                    $scope.latitudeProperty;
                    $scope.mapCityProperty;

                    DataService.getFormDesc(refDomainName).then(function (response) {
                        $scope.fields = response.plain();
                        var i = 0;
                        for (; i < $scope.fields.length; i++) {
                            var field = $scope.fields[i];

                            if (typeof field.isMapInfo !== 'undefined') {
                                if (typeof field.isMapInfoTitle !== 'undefined') {
                                    $scope.mapInfoTitleProperty = field.name;
                                } else {
                                    $scope.mapInfoProperties.push({
                                        name: field.name,
                                        sort: field.mapInfoSort,
                                        title: field.title
                                    });
                                }
                            } else if (typeof field.isLongitude !== 'undefined') {
                                $scope.longitudeProperty = field.name;
                            } else if (typeof field.isLatitude !== 'undefined') {
                                $scope.latitudeProperty = field.name;
                            } else if (typeof field.mapCityProperty !== 'undefined') {
                                $scope.mapCityProperty = {name: field.name, property: field.mapCityProperty};
                            }
                        }

                        $scope.mapInfoProperties = _.sortBy($scope.mapInfoProperties, 'sort');
                        //$scope.mapInfoProperties = _.pluck(_.sortBy($scope.mapInfoProperties, 'sort'), 'name');

                        $scope.canSearch = true;
                    });

                    DataService.getSearchDesc(refDomainName).then(function (response) {
                        $scope.searchFields = response.plain();

                    });

                    loadBaiduMapJScript();

                    $scope.showReference = function () {
                        DialogService.alert(['地图初始化中，请稍后...']);
                    };

                    $scope.$watch('map', function (newValue) {
                        if (typeof newValue !== 'undefined') {
                            var icon = new BMap.Icon('app/components/map/rating.svg', new BMap.Size(40, 40), {
                                //offset: new BMap.Size(10, 25)
                                //imageOffset: new BMap.Size(0, 0 - index * 25)
                            });

                            //var labelWatch = $scope.$watch(function () {
                            //    return $('.BMap_cpyCtrl').length;
                            //}, function (newValue, oldValue) {
                            //    if (newValue > 0) {
                            //        $('.BMap_cpyCtrl').remove();
                            //        $('.anchorBL').remove();
                            //        $('#map_container').removeClass('hidden');
                            //        labelWatch();
                            //    }
                            //});

                            $scope.getInfoWindowContent = function (row) {
                                var content = '<p>';
                                if (typeof $scope.mapInfoTitleProperty !== 'undefined') {
                                    content = content + '<h4>' + row[$scope.mapInfoTitleProperty] + '</h4><br>'
                                }

                                var i = 0;
                                for (; i < $scope.mapInfoProperties.length; i++) {
                                    var infoP = $scope.mapInfoProperties[i];
                                    content = content + '<span>' + infoP.title + ':' + row[infoP.name] + '</span><br>';
                                }

                                content = content + '</p>';

                                return content;
                            };
                            $scope.selectedRow;

                            //$scope.marker;

                            //$scope.infoWindow;

                            $scope.locationClick = function (index) {
                                $('.BMap_pop').remove();
                                $('.BMap_shadow').remove();
                                var location = $scope.results.getPoi(index),
                                    i = 0;

                                var distances = [];
                                for (; i < $scope.page.list.length; i++) {
                                    var row = $scope.page.list[i];

                                    var point = new BMap.Point(row[$scope.longitudeProperty], row[$scope.latitudeProperty]);  // 创建点坐标A--大渡口区
                                    var distance = $scope.map.getDistance(location.point, point).toFixed(2);
                                    distances.push({
                                        point: point,
                                        distance: parseFloat(distance),
                                        title: row[$scope.mapInfoTitleProperty] + '(' + distance + 'm)'
                                    });
                                }

                                var outlets = _.take(_.sortBy(distances, 'distance'), 5);
                                $('#outlet_list').html('');
                                for (var j = 0; j < outlets.length; j++) {

                                    var listItem = '<a ng-click="outletListClick(\''+outlets[j].point.lng + '_' + outlets[j].point.lat+'\')"><label class="align-left" style="padding-left:2px;min-width:15px;width: 15px">' + (1 + j) + '.</label>' + outlets[j].title + '</a>';
                                    $('#outlet_list').append($compile(listItem)($scope));
                                    if (j < outlets.length - 1) {
                                        $('#outlet_list').append('<br>');
                                    }
                                }

                                if (null != $scope.marker) {
                                    $scope.map.removeOverlay($scope.marker);
                                }

                                $scope.marker = new BMap.Marker(location.point); //按照地图点坐标生成标记
                                $scope.map.addOverlay($scope.marker);

                                $scope.map.setViewport([location.point, outlets[0]['point']]);


                            };

                            $scope.marker = null;

                            $scope.outletListClick = function(key){
                                $scope.map.setViewport([$scope.markers[key].point, $scope.marker.point]);
                                $scope.markers[key].K.click();
                            };

                            //console.log($scope)
                            $scope.queryLocation = function () {
                                $('#outlet_list').html('');
                                if ($scope.canSearch) {
                                    //$scope.map.clearOverlays();
                                    //
                                    //// query all outlet within given area
                                    //DataService.getPage(refDomainName, {}, DEFAULT_PAGE)
                                    //    .then(function (response) {
                                    //        $scope.page = response.plain();
                                    //        var i = 0;
                                    //        for (; i < $scope.page.list.length; i++) {
                                    //            (function (row) {
                                    //                var point = new BMap.Point(row[$scope.longitudeProperty], row[$scope.latitudeProperty]);
                                    //                var marker = new BMap.Marker(point, {icon: icon}); //按照地图点坐标生成标记
                                    //                $scope.map.addOverlay(marker);
                                    //                marker.addEventListener("click", function () {
                                    //                    //if($scope.infoWindow) {
                                    //                    //    this.closeInfoWindow($scope.infoWindow);
                                    //                    //}
                                    //                    //$scope.$apply(function(){
                                    //                    $scope.selectedRow = row;
                                    //                    //});
                                    //
                                    //                    //$scope.marker = this;
                                    //                    //$scope.infoWindow = new BMap.InfoWindow($scope.getInfoWindowContent(row));
                                    //                    this.openInfoWindow(new BMap.InfoWindow($scope.getInfoWindowContent(row)));
                                    //                });
                                    //            })($scope.page.list[i]);
                                    //        }
                                    //    });

                                    // markup outlets

                                    // query address location within input throw baidu
                                    var address = $('#map_address_input').val();
                                    if (address !== '') {
                                        var city = $scope.query[$scope.mapCityProperty.name];
                                        if (typeof city !== 'undefined') {
                                            if ($scope.mapCityProperty.property !== '') {
                                                city = city[$scope.mapCityProperty.property];
                                            }
                                        } else {
                                            city = address;
                                        }
                                        var local = new BMap.LocalSearch(city,
                                            {
                                                //renderOptions: {
                                                //    map: $scope.map,
                                                //    autoViewport: true,
                                                //    selectFirstResult: false
                                                //},
                                                //pageCapacity: 8
                                                onSearchComplete: function (results) {
                                                    $('#location_list').html('');
                                                    $scope.results = results;
                                                    // 判断状态是否正确
                                                    if (local.getStatus() == BMAP_STATUS_SUCCESS) {
                                                        //var s = [];

                                                        for (var i = 0; i < results.getCurrentNumPois(); i++) {
                                                            //s.push(results.getPoi(i).title + ", " + results.getPoi(i).address);

                                                            //s.push('<a ng-click="locationClick('+i+')"><label class="align-left" style="padding-left:2px;min-width:15px;width: 15px">'+String.fromCharCode(65+i)+'.</label>'+results.getPoi(i).title+'</a>');
                                                            (function (i) {
                                                                var listItem = '<a ng-click="locationClick(' + i + ')"><label class="align-left" style="padding-left:2px;min-width:15px;width: 15px">' + String.fromCharCode(65 + i) + '.</label>' + results.getPoi(i).title + '</a>';
                                                                $('#location_list').append($compile(listItem)($scope));
                                                                if (i < results.getCurrentNumPois() - 1) {
                                                                    $('#location_list').append('<br>');
                                                                }
                                                            })(i);

                                                        }
                                                        //$('#location_list').append('<span>results.getPoi(i).title</span>');
                                                        //document.getElementById("r-result").innerHTML = s.join("<br/>");
                                                        //document.getElementById("location_list").innerHTML = s.join("<br/>");
                                                        $scope.locationClick(0);
                                                    } else if (local.getStatus() == BMAP_STATUS_UNKNOWN_LOCATION) {
                                                        $('#location_list').append('<span>未找到任何结果!</span>');
                                                    }
                                                }
                                            });
                                        local.search(address);
                                    }


                                    // set location to center
                                } else {
                                    DialogService.alert(['信息获取中，请稍等...']);
                                }

                            };

                            var i = 0;
                            for (; i < $scope.searchFields.length; i++) {
                                var field = $scope.searchFields[i];
                                if ($scope.entity[field.name] !== 'undefined') {
                                    (function (field) {
                                        $scope.query[field.name] = $scope.entity[field.name];
                                        $scope.$watch('query.' + field.name, function () {
                                            $scope.entity[field.name] = $scope.query[field.name];
                                        });
                                        //$scope.$watch('entity.' + field.name, function () {
                                        //    $scope.query[field.name] = $scope.entity[field.name];
                                        //});
                                    })(field);
                                }
                            }
                            //$scope.map.clearOverlays();

                            $scope.$watch(function () {
                                return $(window).width + "_" + $(window).height;
                            }, function () {
                                $scope.$broadcast('dialog.height.changed');
                            }, true);

                            $scope.dialog;

                            $scope.showReference = function (type, domainName) {
                                if (typeof $scope.field.mapAddress !== 'undefined') {
                                    $scope.address = $scope.entity[$scope.field.mapAddress];
                                }
                                $scope.markers = {};
                                DataService.getPage(refDomainName, {}, DEFAULT_PAGE)
                                    .then(function (response) {
                                        $scope.page = response.plain();
                                        var i = 0;
                                        for (; i < $scope.page.list.length; i++) {
                                            (function (row) {
                                                var point = new BMap.Point(row[$scope.longitudeProperty], row[$scope.latitudeProperty]);
                                                var marker = new BMap.Marker(point, {icon: icon}); //按照地图点坐标生成标记
                                                $scope.markers[row[$scope.longitudeProperty] + '_' + row[$scope.latitudeProperty]] = marker;
                                                $scope.map.addOverlay(marker);
                                                marker.addEventListener("click", function () {
                                                    //if($scope.infoWindow) {
                                                    //    this.closeInfoWindow($scope.infoWindow);
                                                    //}
                                                    //$scope.$apply(function(){
                                                    $scope.selectedRow = row;
                                                    //});

                                                    //$scope.marker = this;
                                                    //$scope.infoWindow = new BMap.InfoWindow($scope.getInfoWindowContent(row));
                                                    this.openInfoWindow(new BMap.InfoWindow($scope.getInfoWindowContent(row), {enableMessage:false}));
                                                });
                                            })($scope.page.list[i]);
                                        }

                                        if ($scope.dialog) {
                                            $('#ref-' + $scope.field.name).removeAttr('style');
                                            $scope.dialog.modal('show');
                                        } else {
                                            $scope.dialog = DialogService.dialog({
                                                title: '请选择',
                                                message: $('#ref-' + $scope.field.name).removeAttr('style'),
                                                //message : formElm.removeAttr('style'),
                                                afterClose: function () {
                                                    $('#ref-' + $scope.field.name).attr('style', 'height:0px; display: none                                                                                                                                                                                      ');
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
                                                            if (typeof $scope.selectedRow !== 'undefined') {
                                                                $scope.$apply(function () {
                                                                    $scope.entity[type.refName] = $scope.selectedRow;
                                                                });
                                                                // make scroll correct after modal close
                                                                $scope.dialog.modal('hide');
                                                                $scope.$emit('reference.closed', $scope);
                                                                // make scroll correct after modal close
                                                            } else {
                                                                DialogService.alert(['您没有选择任何地点！']);
                                                                return false;
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        $('.chosen-select').each(function () {
                                            var $this = $(this);
                                            $this.next().css({'width': $this.parent().width()});
                                        });
                                        $('.anchorBL').remove();

                                        $scope.$broadcast('dialog.height.changed');

                                        setTimeout(function () {
                                            if ($scope.address !== '') {
                                                $scope.queryLocation();
                                            }
                                        }, 0);
                                    });
                            };

                            $('.fa-map-marker').removeClass('grey');
                        }
                    });

                    //$scope.map = new BMap.Map("map_container");          // 创建地图实例
                    //
                    //var point = new BMap.Point(116.385, 39.925);  // 创建点坐标
                    //$scope.map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
                    //$scope.map.enableScrollWheelZoom();
                    //$scope.map.addControl(new BMap.NavigationControl());

                    $scope.$watch(function () {
                        return $('#ref-' + $scope.field.name).height();
                    }, function (newValue, oldValue) {
                        $('#ref-' + $scope.field.name).parent().height($('#ref-' + $scope.field.name).height());
                    });

                    $scope.$on('$destroy', function () {
                        $('#ref-' + $scope.field.name).remove();
                    });

                    $scope.clear = function (type) {
                        delete $scope.entity[$scope.field.type.refName];
                    };


                    $scope.$on('reference.closed', function (event, data) {
                        if ($scope !== data) {
                            $('body').addClass('modal-open');
                        }
                    });

                    $scope.$on('dialog.height.changed', function (event, data) {
                        $('#ref-' + $scope.field.name).parent().height($('#ref-' + $scope.field.name).height());
                        $(window).resize();
                    });

                },
                link: function (scope, element, attrs) {

                    $('#map_address_input', element).keydown(function (e) {
                        // Allow: backspace, delete, tab, escape, enter and .
                        if (e.keyCode === 13) {
                            scope.queryLocation();
                        }
                    });
                }
            };
        }])

;