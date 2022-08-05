(function () {

    function settingsController(Font, toastr) {
        var ctrl = this;
        ctrl.status = "Could not fetch status";
        ctrl.$onInit = function () {
            ctrl.checkCache();
        };
        ctrl.saveCache = function () {
            Font.saveCache().then(function (res) {
                if (res.status === 200) {
                    toastr.success('Saved ' + formatSize(res.data), 'Success');
                    ctrl.checkCache();
                }
            });
        };
        ctrl.reloadFonts = function () {
            toastr.info('Reloading fonts', 'Loading');
            Font.reload().then(function (res) {
                if (res.status === 200) {
                    if (res.data) {
                        toastr.success('Successfully reloaded '+res.data.fontCount+' fonts ('+res.data.styleCount+' styles)', 'Success');
                        ctrl.checkCache();
                    } else {
                        toastr.error('Could not load fonts', 'Failed');
                    }
                } else {
                    toastr.error('Could not load fonts', 'Failed');
                }
            }, function (err) {
                if (err) console.error(err);
                toastr.error('Could not load fonts', 'Failed');
            });
        };

        ctrl.deleteCache = function () {
            Font.deleteCache().then(function (res) {
                if (res.status === 200) {
                    if (res.data === true) {
                        toastr.success('Successfully deleted font cache', 'Success');
                        ctrl.checkCache();
                    } else {
                        toastr.error('Could not delete font cache', 'Failed');
                    }
                } else {
                    toastr.error('Could not delete font cache', 'Failed');
                }
            }, function (err) {
                if (err) console.error(err);
                toastr.error('Could not delete font cache', 'Failed');
            });
        };

        ctrl.checkCache = function () {
            Font.checkCache().then(function (res) {
                if (res.status === 200) {
                    if (res.data !== 0) {
                        ctrl.status = "Cache file exists (" + formatSize(res.data) + ")";
                    } else {
                        ctrl.status = "No cache file found (0 bytes)";
                    }
                }
            })
        };

        function formatSize(input) {
            var size = input;
            var prefix = ['','K','M','G'], prefixIndex = 0;
            while (size >= 1000) {
                size = (size / 1000).toFixed(1);
                prefixIndex++;
            }
            return size + ' ' + (prefixIndex === 0 ? 'bytes' : (prefix[prefixIndex]+'b'));
        }
    }

    angular.module('app')
        .controller('SettingsController', settingsController);

})();