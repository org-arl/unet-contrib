import arlpy
import folium


def set_origin(gps_location):
    """
    Convert gps_location to local corrdinate
    and set that as origin.
    :param gps_location: GPS location where the origin needs to be set as origin
    :returns: A tuple (origin in cartesian coordinates, origin in utm coordinates)
    """
    origin_utm = list(arlpy.geo.pos(gps_location))
    origin_cartesian = [j - i for i, j in zip(list(arlpy.geo.pos(gps_location)), list(arlpy.geo.pos(gps_location)))]
    return (origin_cartesian, origin_utm)


def gps_to_local(gps_location, gps_origin):
    """
    Convert gps_location to local corrdinate.
    :param gps_location: GPS location in (lat, long) format
    :param gps_origin: GPS location of the origin in (lat, long) format
    :returns: A tuple (location in cartesian coordinates, location in utm coordinates)
    """
    coordinates = set_origin(gps_origin)
    origin_utm = coordinates[1]
    coord_utm = arlpy.geo.pos(gps_location)
    coord_cartesian = [j - i for i, j in zip(origin_utm, list(coord_utm))]
    return (coord_cartesian, coord_utm)


def local_to_gps(coord_cartesian, gps_origin):
    """
    Convert local corrdinate to gps_location.
    :param coord_cartesian: Cartesian coordniates local
    :param gps_origin: GPS location of the origin in (lat, long) format
    :returns: A tuple GPS location (lat, long) format
    """
    zone = arlpy.geo.zone(gps_origin)
    coord_computed = [coord_cartesian[0], coord_cartesian[1], 0.0]
    utm_coord_computed = [i + j for i, j in zip(set_origin(gps_origin)[1], coord_computed)]
    gps_coord_computed = arlpy.geo.latlong(utm_coord_computed, zone)
    return gps_coord_computed


def plot(locations, gps_origin, ground_truth):
    """
    Plots the node locations and ground truth on folium map.
    :param locations: list of cartesian coordinates (node locations)
    :param gps_origin: GPS location of the origin in (lat, long) format
    :param ground_truth: GPS location of the target node
    :returns: map
    """
    # convert locations from local coordinates to gps coordinates
    gps_locations = [local_to_gps(i, gps_origin) for i in locations]
    m = folium.Map(location=list(gps_origin), zoom_start=20)
    if len(locations) == 2:
        folium.Marker(list(gps_locations[0]), popup='<i> Node 1 </i>').add_to(m)
        folium.Marker(list(gps_locations[1]), popup='<i> Node 2 </i>').add_to(m)
        folium.Marker(list(ground_truth), popup='<i> Node 3 </i>', icon=folium.Icon(color='red', icon='unknown')).add_to(m)
    else:
        folium.Marker(list(gps_locations[0]), popup='<i> Node 1 </i>').add_to(m)
        folium.Marker(list(gps_locations[1]), popup='<i> Node 2 </i>').add_to(m)
        folium.CircleMarker(list(gps_locations[2]), popup='<i> Node3 C Localized </i>', fill=True, color='red').add_to(m)
        folium.CircleMarker(list(gps_locations[3]), popup='<i> Node3 C Localized </i>', fill=True, color='red').add_to(m)
        folium.Marker(list(ground_truth), popup='<i> Node 3 </i>', icon=folium.Icon(color='red', icon='unknown')).add_to(m)
    return m


def flush_modem(modem):
    """
    Clears the buffer in the modem.
    :param modem: A gateway connection handle to the modem
    """
    while modem.receive(timeout=1000):
        pass
