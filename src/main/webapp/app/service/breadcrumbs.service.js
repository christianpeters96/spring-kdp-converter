(function () {

    function breadcrumbsFactory($state, $interpolate) {
        var list = [], title;

        function getProperty(object, path) {
            function index(obj, i) {
                return obj[i];
            }

            return path.split('.').reduce(index, object);
        }

        function addBreadcrumb(title, state, icon) {
            var entry = {
                title: title,
                state: state
            };
            if (icon) {
                entry.icon = icon;
            }
            list.push(entry);
        }

        function generateBreadcrumbs(state) {
            if (state) {
                if(angular.isDefined(state.parent)) {
                    generateBreadcrumbs(state.parent);
                }

                if(angular.isDefined(state.breadcrumb)) {
                    if(angular.isDefined(state.breadcrumb.title)) {
                        var icon, stateName;
                        if (angular.isDefined(state.breadcrumb.icon)) {
                            icon = state.breadcrumb.icon;
                        }
                        stateName = state.name;
                        if (state.abstract) {
                            stateName = "";
                        }
                        addBreadcrumb(state.breadcrumb.title, stateName, icon);
                    }
                }
            }
        }

        function appendTitle(_title, index) {
            var title = _title;

            if(index < list.length - 1) {
                title += ' > ';
            }

            return title;
        }

        function generateTitle() {
            angular.forEach(list, function(breadcrumb, index) {
                title = appendTitle(breadcrumb.title, index);
            });
        }

        return {
            generate: function() {
                list = [];
                generateBreadcrumbs($state.$current);
                generateTitle();
            },

            title: function() {
                return title;
            },

            list: function() {
                return list;
            }
        };
    }

    angular.module('app')
        .factory('Breadcrumbs', breadcrumbsFactory);

})();