(function () {

    function pageBoxDirective() {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                heading: '@'
            },
            templateUrl: 'app/directive/template/page-box.html'
        }
    }

    angular.module('app')
        .directive('pageBox', pageBoxDirective);

})();