(function () {

    function pageWidgetDirective() {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                heading: '@'
            },
            templateUrl: 'app/directive/template/page-widget.html'
        }
    }

    angular.module('app')
        .directive('pageWidget', pageWidgetDirective);

})();