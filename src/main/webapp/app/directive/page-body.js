(function () {

    function pageBodyDirective() {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'app/directive/template/page-body.html'
        }
    }

    angular.module('app')
        .directive('pageBody', pageBodyDirective);

})();