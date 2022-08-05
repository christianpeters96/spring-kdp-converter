(function () {
    function generatorService($http) {

        return {
            generatePdf: function (file) {
                var data = {
                    base64: file.content.replace(/data:.+;base64,/, ''),
                    extraFiles: file.extraFiles, //TODO: replace data:.+;base64, (see above)
                    fileName: file.bookTitles[0],
                    bookTitles: file.bookTitles,
                    fileSuffix: file.fileSuffix,
                    font: {
                        family: file.font.family,
                        style: file.font.style,
                        size: file.font.size,
                        color: {
                            name: file.font.color.name,
                            hex: file.font.color.hex
                        }
                    },
                    bgColor: {
                        name: file.bgColor.name,
                        hex: file.bgColor.hex
                    }
                };
                return $http({
                    method: 'POST',
                    url: '/api/pdf/generate',
                    data: data
                });
            },
            getPercent: function() {
                return $http.get('/api/pdf/status');
            },
            generateMultiplePdf: function (files, folders, callback, errorCallback, callbackStatus) {
                var data = [];
                for (var i = 0; i < files.length; i++) {
                    data.push({
                        base64: files[i].content.replace(/data:.+;base64,/, ''),
                        extraFiles: files[i].extraFiles, //TODO: replace data:.+;base64, (see above)
                        fileName: files[i].bookTitles[0],
                        bookTitles: files[i].bookTitles,
                        fileSuffix: files[i].fileSuffix,
                        font: {
                            family: files[i].font.family,
                            style: files[i].font.style,
                            size: files[i].font.size,
                            color: {
                                name: files[i].font.color.name,
                                hex: files[i].font.color.hex
                            }
                        },
                        bgColor: {
                            name: files[i].bgColor.name,
                            hex: files[i].bgColor.hex
                        }
                    });
                }

                $.ajax({
                    method: 'POST',
                    url: '/api/pdf/generate/multi?createFolders='+folders,
                    data: JSON.stringify(data),
                    xhr: function () {
                        var xhr = new window.XMLHttpRequest();
                        xhr.upload.addEventListener("progress", function (e) {
                            callbackStatus(Math.round(e.loaded / e.total * 100));
                        }, false);

                        xhr.upload.onload = function(e) {
                            callbackStatus(100);
                        };
                        return xhr;
                    },
                    headers: {
                        'Accept': 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    success: callback,
                    error: errorCallback
                });
            }
        }
    }

    angular.module('app')
        .service('Generator', generatorService);
})();