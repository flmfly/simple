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
            categories: ['东城区', '西城区', '朝阳区', '丰台区', '石景山区', '海淀区', '门头沟区', '房山区', '通州区', '顺义区', '昌平区', '大兴区', '怀柔区', '平谷区', '密云县', '延庆县']
        },

        yAxis: [{
            title: {
                text: '单/人/平方公里'
            }
        }, {
            title: {
                text: '%'
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
        series: [ {
            type: 'column',
            name: '人均单密度',
            color: Highcharts.getOptions().colors[0],
            data: [94, 12, 17, 158, 200, 82, 66, 18, 116, 36, 167, 118, 70, 116, 24, 166]
        }, {
            type: 'column',
            name: '及时率',
            yAxis: 1,
            color: Highcharts.getOptions().colors[3],
            data: [88.68,86.94,85.48,85.46,90.28,87.89,90.74,91.57,87.14,91.77,85.13,93.54,90.21,89.97,89.65,86.86],
            marker: {
                lineWidth: 2,
                lineColor: Highcharts.getOptions().colors[3],
                fillColor: 'white'
            }
        }]
    });
});
