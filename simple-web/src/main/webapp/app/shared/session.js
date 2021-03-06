/**
 * Created by Jeffrey on 12/7/14.
 */

angular.module('shared.session', [])

    .provider('Session', function () {
        this.domainName = null;
        this.domainId = null;
        this.query = null;
        this.page = null;
        this.parent = {};

        this.property = null;
        this.propId = null;

        this.domainDesc = null;

        this.$get = function () {
            return this;
        };

        this.setProp = function (property, propId) {
            this.property = property;
            this.propId = propId;
        };

        this.setDomainName = function (domainName) {
            this.domainName = domainName;
        };

        this.setDomainDesc = function (domainDesc) {
            this.domainDesc = domainDesc;
        };

        this.setDomainId = function (domainId) {
            this.domainId = domainId;
        };

        this.setParent = function (parent) {
            this.parent = parent;
        };

        this.saveQueryParams = function (query, page) {
            this.query = query;
            this.page = page;
        };

        this.reset = function () {
            this.domainName = null;
            this.domainId = null;
            this.query = null;
            this.page = null;
            this.parent = {};

            this.property = null;
            this.propId = null;

            this.domainDesc = null;
        }
    })

    .run(function ($rootScope, DataService) {
        DataService.getLoginUser().then(function (response) {
            var rtnData = response.plain();
            if (rtnData.status.code === 200) {
                $rootScope.user = rtnData.data;
            }
        });
    })
;