(function () {
    function advancedSettingsController($scope, $timeout, $uibModalInstance, fileInfo, colorPickerOptions, Color, Font, Name) {
        var ctrl = this;

        $scope.colorPickerOptions = colorPickerOptions;

        $scope.bookTitle = angular.copy(fileInfo.bookTitles[0]);

        $scope.globalFontSettings = false;
        $scope.globalBgColor = false;

        $scope.colors = Color.getAll();

        $scope.font = angular.copy(fileInfo.font);

        $scope.useCustomFontColor = false;
        $scope.useCustomBgColor = false;

        $scope.bgColor = fileInfo.bgColor;

        $scope.changedFontColor = function (evt) {
            if ($scope.font.color.name === 'Custom Color') {
                $scope.useCustomFontColor = true;
            }
        };
        $scope.changedFontColor();

        $scope.changedDocumentColor = function (evt) {
            if ($scope.bgColor.name === 'Custom Color') {
                $scope.useCustomBgColor = true;
            }
        };
        $scope.changedDocumentColor();

        $scope.closeModal = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.confirmSettings = function () {
            if ((typeof $scope.selectedFont.name === "string" && $scope.selectedFont.name.length !== 0) &&
                (typeof $scope.selectedFontStyle === "string" && $scope.selectedFontStyle.length !== 0)) {

                $scope.font.family = $scope.selectedFont.name;
                $scope.font.style = $scope.selectedFontStyle;
            }
            $uibModalInstance.close($scope);
        };

        $scope.selectedFont = {styles:[]};
        $scope.selectedFontStyle = "";

        $scope.extraFiles = fileInfo.extraFiles;

        ctrl.imageForm = {
            file: [],
            type: "Front Cover"
        };

        ctrl.uploadNewImage = function () {
            if (ctrl.imageForm.file.length === 0) return; // TODO: Error
            var reader = new FileReader();
            reader.addEventListener('load', function () {
                var image = new Image();
                image.onload = function () {
                    var elem = angular.element('.image-preview .box-body');
                    var divisor = image.width / elem.width();
                    $scope.extraFiles.push({
                        id: ctrl.imageForm.file[0].name + "-" + Date.now(),
                        base64: reader.result,
                        type: ctrl.imageForm.type,
                        pv: {
                            width: image.width / divisor,
                            height: image.height / divisor
                        }
                    });
                    ctrl.imageForm.file = [];
                };
                image.src = reader.result;
            });
            reader.readAsDataURL(ctrl.imageForm.file[0]);
        };

        ctrl.removeImage = function (id) {
            var array = [];
            for (var i = 0; i < $scope.extraFiles.length; i++) {
                if ($scope.extraFiles[i].id !== id) {
                    array.push($scope.extraFiles[i]);
                }
            }
            $scope.extraFiles = array;
        };

        ctrl.imageSrc = fileInfo.content;
        ctrl.previewSize = {width:0,height:0};
        ctrl.$onInit = function () {
            Font.getAll().then(function (res) {
                if (res.status === 200) {
                    ctrl.fontList = [];
                    var keys = Object.keys(res.data);
                    for (var i = 0; i < keys.length; i++) {
                        var fontObject = {
                            name: keys[i],
                            styles: res.data[keys[i]]
                        };
                        ctrl.fontList.push(fontObject);
                        if (keys[i] === $scope.font.family) {
                            $scope.selectedFont = fontObject;
                            for (var index in res.data[keys[i]]) {
                                if (res.data[keys[i]].hasOwnProperty(index)) {
                                    $scope.selectedFontStyle = res.data[keys[i]][index];
                                }
                            }
                        }
                    }
                }
            });

            $timeout(function () {
                var elem = angular.element('.image-preview .box-body');
                var divisor = fileInfo.width / elem.width();
                ctrl.previewSize.width = fileInfo.width / divisor;
                ctrl.previewSize.height = fileInfo.height / divisor;
            }, 1000);
        };

        ctrl.searchQuery = "";
        ctrl.delayedSearchTimeout = null;
        ctrl.fontList = [];
        ctrl.delayedSearch = function () {
            if (ctrl.delayedSearchTimeout !== null) $timeout.cancel(ctrl.delayedSearchTimeout);
            ctrl.delayedSearchTimeout = $timeout(function () {
                if (ctrl.searchQuery.length >= 2 && ctrl.searchQuery.length <= 12) {
                    Font.search(ctrl.searchQuery).then(function (res) {
                        if (res.status === 200) {
                            ctrl.fontList = [];
                            var keys = Object.keys(res.data);
                            for (var i = 0; i < keys.length; i++) {
                                ctrl.fontList.push({
                                    name: keys[i],
                                    styles: res.data[keys[i]]
                                });
                            }
                        }
                    });
                }
                ctrl.delayedSearchTimeout = null;
            }, 1000);
        };
    }

    angular.module('app')
        .controller('AdvancedSettingsController', advancedSettingsController)
})();