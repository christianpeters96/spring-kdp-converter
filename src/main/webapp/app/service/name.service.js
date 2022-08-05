(function () {

    function nameService($http) {
        return {
            plainFileName: function (name) {
                return name
                    .replace(/\.(?:[Pp][Dd][Ff]|[Pp][Nn][Gg]|[Jj][Pp][Ee][Gg]|[Jj][Pp][Gg])$/, '')
                    .replace(/_(?:white|black|light_gray|gray|dark_gray|red|pink|orange|yellow|green|magenta|cyan|blue|custom)$/, '');
            }
        }
    }

    angular.module('app')
        .service('Name', nameService);
})();