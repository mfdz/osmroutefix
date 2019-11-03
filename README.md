# OSMRouteFix

OSMRouteFix aims to make creating and maintaining OSM route relations easier.
It builds on the GraphHopper routing engine and is released under Apache License 2.0.

OSMRouteFix currently is a proof-of-concept. A demo for the Baden-Wuerttemberg region in Germany
is available at http://osmroutefix.mitfahren-bw.de. Currently only detour routes and PTv2 
public transport routes are supported.

#### Creating a route from scratch
If you want to start a route from scratch, you may use the web ui, choose the route type for the relation
you want to create and add start, via and end points. When done, you may download a changeset template
in level0l format, which you can apply e.g. in the level0-editor. Note that the created template contains
some empty tags which you should either fill or delete.

#### Retracing an existing route
You may enter a route ID of an existing route relation and select "Load". OSMRouteFix will use node members as start/end/via nodes to calculate a route. Dependending on the route type, the following members are used as route points:

* detour: node members with role start, via, end
* public_transport: node members with role stop 

Note: for public_transport, currently only PTv2 routes with stop_positions as stop members are supported. I.e. no snapping of bus_stops or platforms is performed.

### Warning
We think this tool could be helpful for the OSM community to detect and correct errors in route relations. However, you should keep in mind, that automated edits are not intended. Any tagging/mapping error on a feature, every temporarilly added construction or whatever might result in a route result which should not be applied automatically.

### Help wanted
This project is still a prototype. Many features are missing: the BusFlagEncoder only covers a few special cases for bus routing, the BusRouteStrategy could snap bus_stops to the closest highway way, usability of the UI is poor, additional route types could be added... If you deem this tool helpful and want to support it, we'd be glad to receive your pull requests :-) 

### Docker
#### Development
Run a dockerized OSMRouteFix from sources:

```bash
docker build -t osmroutefix:master .
docker run -d --name osmroutefix -v <path_data_directory>/data:/data -p 8989:8989 osmroutefix:master
```

#### Production
Create a dockerized OSMRouteFix containing just pre-built jar (~25MB on top of standard openjdk-image):

Note: The pbf files must already be downloaded to the data directory. The data is not downloades by the container.

```bash
npm run bundleProduction
mnv install docker:build
docker run -d --name osmroutefix -v <path_data_directory>/data:/osmroutefix/data -p 8989:8989 osmroutefix:latest
```

See also the builds at [Docker Hub](https://hub.docker.com/r/mfdz/osmroutefix)


## License
In accordance with the base project GrapHopper we built upon, we chose the Apache License.

Nevertheless, we suggest that you contribute back your changes.
