angular.module('shared.directives.htmlEditor', ['ngCkeditor'])
    .directive('htmlEditor', ['DataService', function (DataService) {
        return {
            restrict: 'A',
            template: '<textarea ckeditor="editorOptions" ng-model="val"></textarea>',
            replace: true,
            scope: {
                val: '='
            },
            controller: function ($scope) {

                $scope.editorOptions = {
                    language: 'zh-cn',
                    image_previewText: ' ',
                    filebrowserImageBrowseUrl: 'assets/libs/fckeditor/editor/filemanager/browser/default/browser.html?Type=Image&Connector=' + __GLOBE.BASE_PATH + '/fckeditor/connector&Origin=' + __GLOBE.ORIGIN,
                    //filebrowserImageUploadUrl: 'http://192.168.11.2:8080/s/fckeditor/connector?Type=Image'
                };

            },
            link: function (scope, element, attrs) {

            }
        };
    }]);