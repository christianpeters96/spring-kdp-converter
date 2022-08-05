(function () {
    function appConfig($locationProvider, $urlRouterProvider, $stateProvider) {
        $locationProvider.hashPrefix('');
        $urlRouterProvider.otherwise('/home');
        $stateProvider
            .state({
                name: 'home',
                url: '/home',
                templateUrl: 'app/page/home/home.html',
                breadcrumb: {
                    icon: 'home',
                    title: 'Home'
                }
            })
            .state({
                name: 'settings',
                url: '/settings',
                templateUrl: 'app/page/settings/settings.html',
                controller: 'SettingsController',
                controllerAs: '$ctrl',
                breadcrumb: { icon: 'wrench', title: 'Settings' }
            })
            .state({
                abstract: true,
                name: 'tools',
                breadcrumb: { icon: 'gear', title: 'Tools' }
            })
            .state({
                name: 'tools.imageToKdp',
                url: '/tools/image-to-kdp',
                templateUrl: 'app/page/tools/image-to-kdp/image-to-kdp.html',
                controller: 'ImageToKdpController',
                controllerAs: '$ctrl',
                breadcrumb: { title: 'Image to KDP' }
            });
    }

    angular.module('app', ['ui.bootstrap', 'color.picker', 'localytics.directives', 'ngAnimate', 'toastr', 'ui.router', 'ngSanitize'])
        .config(appConfig);
})();
