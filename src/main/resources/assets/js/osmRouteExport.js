
module.exports.addOsmRouteExport = function (ghRequest) {
    var dialog;

    function exportOsmRoute() {
        if (ghRequest.route.isResolved())
            window.open(ghRequest.createOsmRouteURL());
        return false;
    }

    function exportOsmRouteFix(osmRouteId) {
        window.open(ghRequest.createOsmRouteFixURL(osmRouteId));
        return false;
    }

    $('#osmRouteType').change(function (e) {
        ghRequest.initRouteType($('#osmRouteType').val());
    });

    $('#osmRouteExportButton a').click(function (e) {
        // no page reload
        e.preventDefault();
        exportOsmRoute();
    });


    $('#osmRouteFixButton a').click(function (e) {
        // no page reload
        e.preventDefault();
        exportOsmRouteFix($('#osmRouteId').val());
    });

};
