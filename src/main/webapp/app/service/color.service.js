(function () {
    function colorService() {
        var colorList = [
            { name: 'White', hex: 'ffffff'},
            { name: 'Black', hex: '0b0b0b'},
            { name: 'Light Gray', hex: 'c0c0c0'},
            { name: 'Gray', hex: '808080'},
            { name: 'Dark Gray', hex: '404040'},
            { name: 'Red', hex: 'ff0000'},
            { name: 'Pink', hex: 'ffafaf'},
            { name: 'Orange', hex: 'ffc800'},
            { name: 'Yellow', hex: 'ffff00'},
            { name: 'Green', hex: '00ff00'},
            { name: 'Magenta', hex: 'ff00ff'},
            { name: 'Cyan', hex: '00ffff'},
            { name: 'Blue', hex: '0000ff'},
            { name: 'Custom Color', hex: 'ffffff'}
        ];

        function getColorByName(name) {
            for (var i = 0; i < colorList.length; i++) {
                if (colorList[i].name.toLowerCase() === name.toLowerCase())
                    return colorList[i];
            }
            return null;
        }

        return {
            byName: function (name) {
                var out = getColorByName(name);
                if (out === null) {
                    out = colorList[0];
                    console.error("can't find request color '" + name + "' - using '" + out.name + "' as fallback");
                }
                return out;
            },
            defaultTextColor: function () {
                return getColorByName('black');
            },
            defaultBackgroundColor: function () {
                return getColorByName('white');
            },
            getAll: function () {
                return colorList;
            }
        }
    }

    angular.module('app')
        .service('Color', colorService);
})();