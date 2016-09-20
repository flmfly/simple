/**
 * Created by Jeffrey on 2/13/15.
 */

$(function () {
    $('#combo-container').highcharts({
        title: {
            text: ''
            //text: 'Combination chart'
        },
        xAxis: {
            categories: ['2014年8月', '2014年9月', '2014年10月', '2014年11月', '2014年12月', '2015年1月', '2015年2月', '2015年3月', '2015年4月', '2015年5月', '2015年6月', '2015年7月']
        },

        yAxis: [{
            title: {
                text: '万元'
            }
        }, {
            title: {
                text: '利润率(%)'
            },
            opposite: true
        }],
        //labels: {
        //    items: [{
        //        html: 'Total fruit consumption',
        //        style: {
        //            left: '50px',
        //            top: '18px',
        //            color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
        //        }
        //    }]
        //},
        credits: {
            href: "http://www.cpscs.cc/",
            text: "www.cpscs.cc"
        },
        series: [{
            type: 'column',
            name: '收入',
            data: [662.40,1204.32,1550.08,1122.88,402.56,1346.40,102.72,1181.60,614.88,594.40,1262.40,459.84]
        }, {
            type: 'column',
            name: '利润',
            color: Highcharts.getOptions().colors[2],
            data: [172.22,308.90,339.39,381.63,90.97,417.81,26.91,465.34,124.44,198.66,474.08,163.45]
        }, {
            type: 'spline',
            name: '利润率',
            yAxis: 1,
            color: Highcharts.getOptions().colors[3],
            data: [26.00,25.65,21.89,33.99,22.60,31.03,26.20,39.38,20.24,33.42,37.55,35.54],
            marker: {
                lineWidth: 2,
                lineColor: Highcharts.getOptions().colors[3],
                fillColor: 'white'
            }
        }]
    });
});
