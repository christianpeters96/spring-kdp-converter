(function () {
    function fontService($http) {
        return {
            getAll: function (query) {
                return $http.get("/api/font/list");
            },
            search: function (query) {
                return $http.get("/api/font/search/"+query+"/20");
            },
            reload: function () {
                return $http.post("/api/font/reload");
            },
            checkCache: function () {
                return $http.get("/api/font/cache");
            },
            saveCache: function () {
                return $http.put("/api/font/cache");
            },
            deleteCache: function () {
                return $http.delete("/api/font/cache");
            }
        }
    }

    angular.module('app')
        .service('Font', fontService);
})();