(function () {
    angular.module('OrderViewer').directive('orderViewerMap', ['gisHelper', '$filter', function (gisHelper, $filter) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                missions: '=',
                zones: '=',
                focusDrone: '='
            },
            template: '<div id="map" class="map"></div>',
            link: function (scope) {

                function initialize() {
                    scope.route = scope.missions[0].route.wayPoints;
                    scope.allDroneInfos = flatDroneInfosToOneArray();
                    createMap();
                    addRouteLayer(scope.route);
                    scope.missionLayers = addFlownRouteLayers(scope.missions);
                    addPopupOverlay();
                    addMapInteractions();
                    addDroneInfoEventListener();
                    addMissionListener();
                }

                function addMissionListener() {
                    scope.$watch('missions', function (newValue, oldValue) {
                        newValue.forEach(function (mission) {
                            var missionLayer = scope.missionLayers[mission.id];
                            var layerIsAlreadyShown = gisHelper.isLayerOnMap(scope.map, missionLayer);

                            if (mission.active && !layerIsAlreadyShown) {
                                scope.map.addLayer(missionLayer);
                            } else if(!mission.active && layerIsAlreadyShown) {
                                scope.map.removeLayer(missionLayer);
                            }
                        });
                    }, true);
                }

                function addPopupOverlay() {
                    scope.popup = new ol.Overlay.Popup();
                    scope.map.addOverlay(scope.popup);
                }

                function createMap() {
                    var mapLayers = gisHelper.getBaseAndSatelliteLayer();

                    scope.format = new ol.format.WKT();

                    scope.vectorLayer = createVectorLayer();

                    scope.map = new ol.Map({
                        target: 'map',
                        layers: [mapLayers, scope.vectorLayer],
                        view: new ol.View({
                            center: [981481.3, 5978619.7],
                            zoom: 18
                        })
                    });

                    scope.map.getView().fit(scope.vectorLayer.getSource().getExtent(), scope.map.getSize());

                    var layerSwitcher = new ol.control.LayerSwitcher({
                        tipLabel: 'Legende'
                    });

                    scope.map.addControl(layerSwitcher);
                }

                function addRouteLayer(calculatedRoute) {
                    var coordinates = gisHelper.convertRouteToCoordinates(calculatedRoute);
                    scope.routeLayer = createRouteLayerWithRouteLine(coordinates);
                    scope.routeMarkers = gisHelper.createRouteMarkers(calculatedRoute);
                    scope.routeLayer.getSource().addFeatures(scope.routeMarkers);
                    scope.map.addLayer(scope.routeLayer);
                }


                function addFlownRouteLayers(missions) {
                    var missionLayers = {};

                    missions.forEach(function (mission) {
                        var droneInfos = filterEmptyPositions(mission);
                        var coordinates = gisHelper.convertDroneInfosToCoordinates(droneInfos);
                        var routeLayer = createRouteLayerWithRouteLine(coordinates, flownRouteStyle);
                        var routeMarkers = gisHelper.createDroneInfoMarkers(droneInfos);
                        routeLayer.getSource().addFeatures(routeMarkers);
                        scope.map.addLayer(routeLayer);
                        missionLayers[mission.id] = routeLayer;
                    });

                    return missionLayers;
                }

                function filterEmptyPositions(mission) {
                    return mission.droneInfos.filter(function (droneInfo) {
                        return droneInfo.gpsState.posLat != 0.0 || droneInfo.gpsState.posLon != 0.0;
                    });
                }

                function createRouteLayerWithRouteLine(coordinates, routeStyle) {
                    return new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: [new ol.Feature({
                                geometry: new ol.geom.LineString(coordinates, 'XY'),
                                name: 'Line'
                            })]
                        }),
                        style: routeStyle || gisHelper.getRouteStyle()
                    });
                }

                function createVectorLayer() {
                    scope.readOnlyFeatures = gisHelper.getFeaturesFromZones(scope.zones, scope.format);

                    return new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: scope.readOnlyFeatures
                        }),
                        style: polygonStyle
                    });
                }

                function addNewRouteMarker(data) {
                    var missionLayer = scope.missionLayers[data.missionId];
                    var coordinates = gisHelper.convertDroneInfoToCoordinate(data.droneInfo);
                    var marker = gisHelper.createDroneInfoMarker(coordinates, data.droneInfo.id);
                    missionLayer.getSource().addFeature(marker);
                    scope.allDroneInfos.push(data.droneInfo);
                    return coordinates;
                }

                function addDroneInfoEventListener() {
                    scope.$on('DroneInfoReceived', function (event, data) {
                        var coordinates = addNewRouteMarker(data);
                        if (scope.focusDrone) {
                            gisHelper.panTo(scope.map, coordinates);
                        }
                    });
                }

                function addMapInteractions() {

                    scope.map.on('pointermove', function (evt) {
                        scope.map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                            if (feature.getId()) {
                                var foundDroneInfo = scope.allDroneInfos.filter(function (droneInfo) {
                                    return droneInfo.id == feature.getId();
                                });

                                if (foundDroneInfo[0]) {
                                    var i = foundDroneInfo[0];
                                    var popupHtml =
                                        '<div>' +
                                        '<div>Time: ' + $filter('date')(i.clientTime, 'dd.MM.yyyy HH:mm:ss') + '</div>' +
                                        '<div>Battery: ' + i.batteryState.remain + '%<div>' +
                                        '<div>Voltage: ' + i.batteryState.voltage + 'V<div>' +
                                        '<div>Discharge: ' + i.batteryState.discharge.toFixed(2) / 100 + 'A<div>' +
                                        '<div>Speed: ' + i.droneState.groundSpeed.toFixed(2) + ' m/s<div>' +
                                        '<div>vertical Speed: ' + i.droneState.verticalSpeed.toFixed(2) + ' m/s<div>' +
                                        '<div>Altitude: ' + i.droneState.altitude.toFixed(2) + 'm<div>' +
                                        '<div>target Altitude: ' + i.droneState.targetAltitude.toFixed(2) + 'm<div>' +
                                        '</div>';


                                    scope.popup.show(evt.coordinate, popupHtml);
                                } else {
                                    var foundWayPoint = scope.route.filter(function (wayPoint) {
                                        return wayPoint.id == feature.getId();
                                    });

                                    if (foundWayPoint[0]) {
                                        var popupHtml =
                                            '<div>' +
                                            '<div>Altitude: ' + foundWayPoint[0].position.height + 'm</div>' +
                                            '<div>Action: ' + foundWayPoint[0].action + '<div>' +
                                            '</div>';
                                        scope.popup.show(evt.coordinate, popupHtml);
                                    }
                                }
                            }
                        });

                    });
                }

                function flatDroneInfosToOneArray() {
                    var droneInfoArrays = scope.missions.map(function (mission) {
                        return mission.droneInfos;
                    });

                    return [].concat.apply([], droneInfoArrays);
                }


                function polygonStyle(feature) {
                    return [gisHelper.getZoneStyle(gisHelper.getZoneById(scope.zones, feature.getId()))];
                }

                function flownRouteStyle() {
                    return new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: gisHelper.flownRouteColor,
                            width: 1
                        })
                    });
                }

                initialize();
            }
        };
    }]);
})();