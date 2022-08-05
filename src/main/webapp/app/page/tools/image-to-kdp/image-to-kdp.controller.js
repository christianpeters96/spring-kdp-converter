(function () {

    function imageToKdpController($scope, $timeout, $interval, $uibModal, toastr, Generator, Color) {
        var ctrl = this;
        ctrl.files = [];
        ctrl.uploadedFiles = [];

        ctrl.colorPickerOptions = {
            format: 'hex',
            allowEmpty: false
        };

        ctrl.canGenerateMultiple = false;
        ctrl.useSeparatedFolders = false;

        ctrl.$onInit = function () {
            $interval(ctrl.checkStatus, 100);
        };

        ctrl.checkStatus = function () {
            Generator.getPercent().then(function (res) {
                if (res.status === 200) {
                    ctrl.generationState = "Generating";
                    ctrl.generationStatus = res.data.value;
                    ctrl.updatePercentage();
                }
                if (res.status === 202) {
                    ctrl.generationStatus = 0;
                }
            });
        };

        ctrl.selectFiles = function () {
            var input = angular.element('input.file-uploader');
            input.click();
        };
        ctrl.uploadFiles = function () {
            $timeout(function() {
                for (var i = 0; i < ctrl.files.length; i++) {
                    if (/image\/(?:png|jpeg)/.test(ctrl.files[i].type)) {
                        addFile(ctrl.files[i]);
                    }
                    // TODO: toastr error when file does not match type
                }
            }, 500);
        };
        ctrl.setFontSettingsGlobal = function(settings) {
            for (var i = 0; i < ctrl.uploadedFiles.length; i++) {
                ctrl.uploadedFiles[i].font = settings;
            }
        };
        ctrl.setBgColorGlobal = function(color) {
            for (var i = 0; i < ctrl.uploadedFiles.length; i++) {
                ctrl.uploadedFiles[i].bgColor = color;
            }
        };
        function getUploadableCheckedFiles() {
            var output = [];
            for (var i = 0; i < ctrl.uploadedFiles.length; i++) {
                if (ctrl.uploadedFiles[i].checked) {
                    if (ctrl.uploadedFiles[i].bookTitles.length !== 0) {
                        for (var bti in ctrl.uploadedFiles[i].bookTitles) {
                            var title = ctrl.uploadedFiles[i].bookTitles[bti];
                            if (title.length !== 0) {
                                output.push(ctrl.uploadedFiles[i]);
                                break;
                            }
                        }
                    }
                }
            }
            return output;
        }
        ctrl.remove = function (identifier) {
            var newList = [];
            for (var i = 0; i < ctrl.uploadedFiles.length; i++) {
                if (ctrl.uploadedFiles[i].identifier !== identifier)
                    newList.push(ctrl.uploadedFiles[i])
            }
            ctrl.uploadedFiles = newList;
        };
        ctrl.generating = false;
        ctrl.generationState = "";
        ctrl.uploadStatus = 0;
        ctrl.generationStatus = 0;
        ctrl.statusPercentage = 0;
        ctrl.updatePercentage = function () {
            ctrl.statusPercentage = (ctrl.uploadStatus + ctrl.generationStatus) / 2;
        };
        ctrl.convert = function (identifier) {
            var img = ctrl.getImage(identifier);
            var imageSource = img.content.replace(/data:.+;base64,/, '');
            Generator.generatePdf(img).then(function (res) {
                if (res.status === 200) {
                    var contentType = res.headers('Content-Type');
                    var binaryString = window.atob(res.data);
                    var binaryLen = binaryString.length;
                    var bytes = new Uint8Array(binaryLen);
                    for (var i = 0; i < binaryLen; i++) {
                        bytes[i] = binaryString.charCodeAt(i);
                    }
                    var blob = new Blob([bytes], {type: contentType});
                    var link = document.createElement('a');
                    link.href = window.URL.createObjectURL(blob);

                    link.download = img.bookTitles[0] + (img.fileSuffix ? img.fileSuffix : "") + " " + img.bgColor.name + "." + (contentType.replace(/application\//, ''));
                    link.click();
                } else {
                    toastr.error('Something went wrong while file generation (Status: ' + res.status + ')', 'Generation failed');
                }
            }, function (err) {
                toastr.error(err.data.message, 'Error (Status: ' + err.status + ')');
            });
        };
        ctrl.convertMultiple = function () {
            var files = getUploadableCheckedFiles();
            for (var fi in files) {
                if (files.hasOwnProperty(fi)) {
                    for (var bi in files[fi].bookTitles) {
                        if (files[fi].bookTitles.hasOwnProperty(bi)) {
                            var bookTitle = files[fi].bookTitles[bi];
                            if (bookTitle.length === 0) {
                                toastr.error('Missing book title for image "' + files[fi].name + '". Please check your fields.', 'Error');
                                return;
                            }
                        }
                    }
                }
            }

            ctrl.generating = true;
            Generator.generateMultiplePdf(getUploadableCheckedFiles(), ctrl.useSeparatedFolders, function (res) {
                ctrl.generating = false;
                ctrl.uploadStatus = 0;
                ctrl.generationStatus = 0;
                ctrl.statusPercentage = 0;
                var binaryString = window.atob(res);
                var binaryLen = binaryString.length;
                var bytes = new Uint8Array(binaryLen);
                for (var i = 0; i < binaryLen; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }
                var blob = new Blob([bytes], {type: "application/zip"});
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);

                link.download = 'bundle.zip';
                link.click();

            }, function (err) {
                if (err) console.error(err);
                toastr.error(err.responseJSON.message, 'Error (Status: ' + err.status + ')');
                ctrl.generating = false;
            }, function (status) {
                ctrl.uploadStatus = status;
                if (status === 100) {
                    ctrl.generationState = "";
                } else {
                    ctrl.generationState = "Uploading";
                }
                ctrl.updatePercentage();
                $scope.$apply();
            });
        };


        ctrl.getImage = function (identifier) {
            for (var i = 0; i < ctrl.uploadedFiles.length; i++) {
                if (ctrl.uploadedFiles[i].identifier === identifier) {
                    return ctrl.uploadedFiles[i];
                }
            }
            return [];
        };

        ctrl.checkUploadedFiles = function () {
            ctrl.canGenerateMultiple = getUploadableCheckedFiles().length > 1;
        };

        function addFile(file) {
            var reader = new FileReader();
            reader.addEventListener('load', function () {
                file.content = reader.result;
                var image = new Image();
                image.onload = function () {
                    file.bookTitles = [""];
                    file.bgColor = Color.defaultBackgroundColor();
                    file.font = {
                        family: 'Roboto',
                        style: 'Regular',
                        color: Color.defaultTextColor(),
                        size: 8
                    };
                    file.extraFiles = [];

                    file.checked = true;

                    // methods
                    file.check = function () {
                        file.checked = !file.checked;
                        ctrl.checkUploadedFiles();
                    };
                    file.advancedSettings = function (event) {
                        var modal = $uibModal.open({
                            templateUrl: 'app/modal/advanced.modal.html',
                            controller: 'AdvancedSettingsController',
                            controllerAs: '$ctrl',
                            backdrop: 'static',
                            size: 'lg',
                            keyboard: false,
                            resolve: {
                                fileInfo: function () {
                                    return file;
                                },
                                colorPickerOptions: function () {
                                    return ctrl.colorPickerOptions;
                                }
                            }
                        });
                        modal.result.finally(angular.noop).then(angular.noop, angular.noop);
                        modal.result.then(function (result) {
                            if (result.globalFontSettings === true) {
                                ctrl.setFontSettingsGlobal(result.font);
                            }
                            file.font = result.font;

                            if (result.globalBgColor === true) {
                                ctrl.setBgColorGlobal(result.bgColor);
                            }
                            file.bgColor = result.bgColor;

                            file.extraFiles = result.extraFiles;
                            file.bookTitles[0] = result.bookTitle;
                        }, function() {});
                    };

                    file.width = image.width;
                    file.height = image.height;

                    var divisor = file.height / 32;
                    if (file.width >= file.height) {
                        divisor = file.width / 32;
                    }
                    file.preview = {
                        width: file.width / divisor,
                        height: file.height / divisor
                    };

                    file.identifier = file.name + "-" + file.lastModified + "-" + Date.now();

                    ctrl.uploadedFiles.push(file);
                    ctrl.canGenerateMultiple = getUploadableCheckedFiles().length > 1;
                    $scope.$apply();
                };
                image.src = file.content;
            });
            reader.readAsDataURL(file);
        }

        ctrl.addBookTitle = function (file, $index) {
            var newArray = [];
            for (var index in file.bookTitles) {
                if (file.bookTitles.hasOwnProperty(index)) {
                    newArray.push(file.bookTitles[index]);
                    if (Number(index) === Number($index)) {
                        newArray.push("");
                    }
                }
            }
            file.bookTitles = newArray;
        };

        ctrl.removeBookTitle = function (file, $index) {
            var newArray = [];
            for (var index in file.bookTitles) {
                if (file.bookTitles.hasOwnProperty(index)) {
                    if (Number(index) !== Number($index)) {
                        newArray.push(file.bookTitles[index]);
                    }
                }
            }
            file.bookTitles = newArray;
        };

        ctrl.enableSuffix = function (event, file) {
            event.preventDefault();
            file.fileSuffix = "_suffix";
        }
    }

    angular.module('app')
        .controller('ImageToKdpController', imageToKdpController);

})();