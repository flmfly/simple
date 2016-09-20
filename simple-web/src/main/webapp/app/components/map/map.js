/**
 * Created by Jeffrey on 3/19/15.
 */

angular.module('components.map', [])

    .controller('MapCtrl', ['$scope', 'DataService', 'DialogService', 'Session',
        function ($scope, DataService, DialogService, Session) {
            $scope.query = Session.query === null ? {} : Session.query;
            Session.query = null;

            $scope.canSearch = false;

            $scope.address = '惠新西街南口';

            $scope.domain = {
                domainName: Session.domainName
            };

            $scope.mapInfoProperties = [];

            $scope.longitudeProperty;
            $scope.latitudeProperty;
            $scope.mapInfoTitleProperty;

            var map = new BMap.Map("map_container");          // 创建地图实例
            var point = new BMap.Point(116.404, 39.915);  // 创建点坐标
            map.centerAndZoom(point, 15);                 // 初始化地图，设置中心点坐标和地图级别
            //map.enableScrollWheelZoom();
            map.addControl(new BMap.NavigationControl());

            var icon = new BMap.Icon('app/components/map/geo_fence.svg', new BMap.Size(26, 26), {
                offset: new BMap.Size(10, 25)
                //imageOffset: new BMap.Size(0, 0 - index * 25)
            });

            DataService.getFormDesc($scope.domain.domainName).then(function (response) {
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
                    }
                }

                $scope.mapInfoProperties = _.sortBy($scope.mapInfoProperties, 'sort');
                //$scope.mapInfoProperties = _.pluck(_.sortBy($scope.mapInfoProperties, 'sort'), 'name');

                $scope.canSearch = true;
            });


            var reqPage = {
                pageNumber: -1,
                pageSize: -1,
                sort: ''
            };

            var labelWatch = $scope.$watch(function () {
                return $('.BMap_cpyCtrl').length;
            }, function (newValue, oldValue) {
                if (newValue > 0) {
                    $('.BMap_cpyCtrl').remove();
                    $('.anchorBL').remove();
                    $('#map_container').removeClass('hidden');
                    labelWatch();
                }
            });

            DataService.getPage(Session.domainName, $scope.query, reqPage)
                .then(function (response) {
                    $scope.page = response.plain();
                    if ($scope.page.total % $scope.page.pageSize === 0) {
                        $scope.page.pageTotal = $scope.page.total / $scope.page.pageSize;
                    } else {
                        $scope.page.pageTotal = Math.floor($scope.page.total / $scope.page.pageSize) + 1;
                    }
                    $scope.$broadcast('grid.data.update', {name: $scope.domain.domainName, d: $scope.page});
                });

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

            $scope.queryLocation = function () {

                if ($scope.canSearch) {
                    map.clearOverlays();

                    // query all outlet within given area
                    DataService.getPage(Session.domainName, $scope.query, reqPage)
                        .then(function (response) {
                            $scope.page = response.plain();
                            var i = 0;
                            for (; i < $scope.page.list.length; i++) {
                                var row = $scope.page.list[i];
                                var point = new BMap.Point(row[$scope.longitudeProperty], row[$scope.latitudeProperty]);
                                var marker = new BMap.Marker(point, {icon: icon}); //按照地图点坐标生成标记
                                map.addOverlay(marker);
                                marker.addEventListener("click", function () {
                                    this.openInfoWindow(new BMap.InfoWindow($scope.getInfoWindowContent(row)));
                                });
                            }
                        });

                    // markup outlets

                    // query address location within input throw baidu
                    var address = $('#map_address_input').val();
                    if (address !== '') {
                        var local = new BMap.LocalSearch(address,
                            {renderOptions: {map: map, autoViewport: false, selectFirstResult: true}, pageCapacity: 8});
                        local.search(address);
                    }


                    // set location to center
                } else {
                    DialogService.alert(['信息获取中，请稍等...']);
                }

            };
        }])

;