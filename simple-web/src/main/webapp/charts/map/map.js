/**
 * Created by Jeffrey on 8/10/15.
 */
$(function () {
    Highcharts.setOptions({
        lang: {
            drillUpText: "返回 > {series.name}"
        }
    });
    var data = Highcharts.geojson(Highcharts.maps['countries/cn/custom/cn-all-china']), small = $('#map_container').width() < 400;
    // 给城市设置随机数据
    $.each(data, function (i) {
        this.drilldown = this.properties['drill-key'];
        this.value = Math.floor(Math.random() * 100000);
    });
    //初始化地图
    $('#map_container').highcharts('Map', {
        chart: {
            events: {
                drilldown: function (e) {
                    if (!e.seriesOptions) {
                        var chart = this;
                        chart.showLoading('<i class="icon-spinner icon-spin icon-3x"></i>');

                        // 加载城市数据
                        $.ajax({
                            type: "GET",
                            url: "assets/libs/highmaps/mapdata/china/json/" + e.point.drilldown + ".geo.json",
                            contentType: "application/json; charset=utf-8",
                            dataType: 'json',
                            crossDomain: true,
                            success: function (json) {
                                data = Highcharts.geojson(json);
                                $.each(data, function (i) {
                                    this.value = Math.floor(Math.random() * 10000);
                                });
                                chart.hideLoading();

                                chart.addSeriesAsDrilldown(e.point, {
                                    name: e.point.properties['cn-name'],
                                    data: data,
                                    dataLabels: {
                                        enabled: true,
                                        format: '{point.properties.name}'
                                    }
                                });

                            },
                            error: function (XMLHttpRequest, textStatus, errorThrown) {

                            }
                        });
                    }


                    this.setTitle(null, {text: e.point.properties["cn-name"]});
                },
                drillup: function () {
                    this.setTitle(null, {text: '中国'});
                }
            }
        },
        credits: {
            href: "http://www.cpscs.cc/",
            text: "www.cpscs.cc"
        },
        title: {
            text: '全国揽投业务量分布图'
        },
        subtitle: {
            text: '中国',
            floating: true,
            align: 'right',
            y: 50,
            style: {
                fontSize: '16px'
            }
        },
        legend: small ? {} : {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle'
        },
        tooltip: {
            pointFormatter: function () {
                if (this.properties['cn-name']) {
                    return this.properties['cn-name'] + ":" + this.value;
                } else {
                    return this.properties.name + ":" + this.value;
                }
            }
        },
        colorAxis: {
            min: 0,
            minColor: '#E6E7E8',
            maxColor: '#005645'
        },
        mapNavigation: {
            enabled: true,
            buttonOptions: {
                verticalAlign: 'bottom'
            }
        },
        plotOptions: {
            map: {
                states: {
                    hover: {
                        color: '#EEDD66'
                    }
                }
            }
        },
        series: [{
            data: data,
            name: '中国',
            dataLabels: {
                enabled: true,
                format: '{point.properties.cn-name}'
            }
        }],
        drilldown: {
            activeDataLabelStyle: {
                color: '#FFFFFF',
                textDecoration: 'none',
                textShadow: '0 0 3px #000000'
            },
            drillUpButton: {
                relativeTo: 'spacingBox',
                position: {
                    x: 0,
                    y: 60
                }
                //,
                //theme: {
                //    fill: 'white',
                //    'stroke-width': 1,
                //    stroke: 'silver',
                //    r: 0,
                //    states: {
                //        hover: {
                //            fill: '#bada55'
                //        },
                //        select: {
                //            stroke: '#039',
                //            fill: '#bada55'
                //        }
                //    }
                //}
            }
        }
    });
});