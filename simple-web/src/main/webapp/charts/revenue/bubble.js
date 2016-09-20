/**
 * Created by Jeffrey on 2/13/15.
 */

$(function () {
    var data = [{x: 3547, y: 22.83, z: 4.06, name: '大兴区'},{x: 2856, y: 29.46, z: 5.08, name: '通州区'},{x: 2010, y: 95.03, z: 21.98, name: '怀柔区'},{x: 3174, y: 104.72, z: 20.98, name: '东城区'},{x: 2448, y: 68.65, z: 19.72, name: '丰台区'},{x: 3758, y: 18.62, z: 3.06, name: '房山区'},{x: 4299, y: 83.23, z: 13.84, name: '石景山区'},{x: 3099, y: 66.91, z: 19.76, name: '门头沟区'},{x: 2786, y: 17.38, z: 3.46, name: '顺义区'},{x: 3335, y: 13.87, z: 2.10, name: '密云县'},{x: 4659, y: 15.14, z: 3.41, name: '平谷区'},{x: 4311, y: 104.35, z: 18.52, name: '海淀区'},{x: 4134, y: 76.80, z: 18.75, name: '西城区'},{x: 4766, y: 46.46, z: 7.63, name: '延庆县'},{x: 4370, y: 94.09, z: 25.96, name: '朝阳区'},{x: 3505, y: 52.54, z: 9.35, name: '昌平区'}];
    $('#bubble-container').highcharts({
        chart: {
            type: 'bubble',
            zoomType: 'xy'
        },

        title: {
            text: ''
        },

        xAxis: {
            //min: 0,
            title: {
                text: '总业务量(单)'
            }
        },
        yAxis: {
            //min: 0,
            title: {
                text: '营收(万元)'
            }
            //,
            //stackLabels: {
            //    enabled: true,
            //    style: {
            //        fontWeight: 'bold',
            //        color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
            //    }
            //}
        },

        legend: {
          enabled: false
        },

        tooltip: {
            headerFormat: '',
            pointFormatter: function () {
                return '<b>'+ this.name + '</b><br>总业务量:' + this.x + '单<br>' + '总收入:' + this.y + '万元<br>' + '利润:' + this.z + '万元';
            }
        },

        series: [{
            name: '北京市',
            type: 'bubble',
            color: Highcharts.getOptions().colors[2],
            data: data,
            dataLabels: {
                enabled: true,
                format: '{point.name}'
            }
        }],
        credits: {
            href: "http://www.cpscs.cc/",
            text: "www.cpscs.cc"
        }
    });
});
