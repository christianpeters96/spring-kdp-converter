(function () {

    function pageTitleDirective($timeout, Breadcrumbs) {
        return {
            restrict: 'E',
            scope: {
                heading: '@',
                sub: '@'
            },
            link: function($scope) {
                Breadcrumbs.generate();
                $scope.bcList = Breadcrumbs.list();
            },
            templateUrl: 'app/directive/template/page-title.html'
        }
    }

    angular.module('app')
        .directive('pageTitle', pageTitleDirective);

})();