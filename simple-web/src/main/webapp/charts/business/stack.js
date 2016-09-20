/**
 * Created by Jeffrey on 2/13/15.
 */

$(function () {
    $('#stack-container').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云县', '延庆县']
        },
        yAxis: [{
            min: 0,
            title: {
                text: '业务量(单)'
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        }],
        //legend: {
        //    align: 'right',
        //    x: -30,
        //    verticalAlign: 'top',
        //    y: 25,
        //    floating: true,
        //    backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || 'white',
        //    borderColor: '#CCC',
        //    borderWidth: 1,
        //    shadow: false
        //},
        tooltip: {
            formatter: function () {
                return '<b>' + this.x + '</b><br/>' +
                    this.series.name + ': ' + this.y + '<br/>' +
                    '总单量: ' + this.point.stackTotal;
            }
        },
        plotOptions: {
            column: {
                stacking: 'normal',
                dataLabels: {
                    enabled: true,
                    color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
                    style: {
                        textShadow: '0 0 3px black'
                    }
                }
            }
        },
        series: [{
            name: '延误',
            color: Highcharts.getOptions().colors[2],
            data: [94, 12, 17, 158, 200, 82, 66, 18, 116, 36, 167, 118, 70, 116, 24, 166]
        }, {
            name: '正常',
            color: Highcharts.getOptions().colors[0],
            data: [3045, 4959, 1995, 4189, 4714, 2627, 4347, 4957, 2322, 3304, 3690, 4493, 2918, 4350, 2296, 3478]
        }, {
            name: '压单',
            color: Highcharts.getOptions().colors[1],
            data: [2, 2, 0, 0, 2, 0, 2, 4, 2, 2, 1, 2, 1, 1, 1, 1]
        }],
        credits: {
            href: "http://www.cpscs.cc/",
            text: "www.cpscs.cc"
        }
    });
});
