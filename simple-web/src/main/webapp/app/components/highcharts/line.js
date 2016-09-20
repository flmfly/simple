/**
 * Created by Jeffrey on 2/13/15.
 */
$(function () {
    $('#line-container').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: ''
            //text: 'Monthly Average Temperature',
            //x: -20 //center
        },
        //subtitle: {
        //    text: 'Source: WorldClimate.com',
        //    x: -20
        //},
        xAxis: {
            categories: ['河南', '重庆', '陕西', '湖南', '四川', '上海', '山东', '宁夏']
        },
        yAxis: {
            min: 0,
            title: {
                text: '签收单（票）'
            },
            stackLabels: {
                enabled: true,
                style: {
                    fontWeight: 'bold',
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                }
            }
        },
        tooltip: {
            formatter: function () {
                return '<b>' + this.x + '</b><br/>' +
                    this.series.name + ': ' + this.y + '<br/>' +
                    'Total: ' + this.point.stackTotal;
            }
        },
        legend: {
            align: 'right',
            x: 0,
            verticalAlign: 'top',
            y: 5,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || 'white',
            borderColor: '#CCC',
            borderWidth: 1,
            shadow: false
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
        credits: {
            enabled: false
        },
        series: [{
            name: '已返',
            data: [440, 302, 425, 601, 483, 581, 399, 171]
        }, {
            name: '未返',
            data: [6, 1, 1, 3, 48, 10, 7, 5]
        }]
    });
});