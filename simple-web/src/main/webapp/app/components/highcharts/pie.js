/**
 * Created by Jeffrey on 2/13/15.
 */

$(function () {
    $('#pie-container').highcharts({
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,//null,
            plotShadow: false
        },
        title: {
            text: ''
            //text: 'Browser market shares at a specific website, 2014'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                    }
                }
            }
        },
        credits: {
            enabled: false
        },
        series: [{
            type: 'pie',
            name: '货量占比',
            data: [
                ['正常采购',    43.0],
                ['物料',       8.0],
                {
                    name: '苹果大件',
                    y: 3.0,
                    sliced: true,
                    selected: true
                },
                ['小米提货',    46.0]
            ]
        }]
    });
});
