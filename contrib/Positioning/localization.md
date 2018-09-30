

```python
import arlpy
import folium
from unetpy import *
from sympy import *
```



# IP addresses of the surface modems

The surface modems available on the some TCP network are identified by the IP addresses and can be accessed. The list 
of IP addresses of the reference modems that are used in this localization application are as hown below:


```python
# List of IP addresses of the depolyed modems

ip_address = ['localhost', 'localhost']
```

# GPS locations of the surface modems

At the point of deployment of the reference modems, the GPS location can be measured to know the axact location of deployment. For the example considered for this demonstration, the GPS locations are as follows:


```python
# List of GPS locations of the deployed modems (optional)

lat1 = 43.933130
long1 = 15.443915
lat2 = 43.933235
long2 = 15.444071
gps_location = [(lat1, long1), (lat2, long2)]
ground_truth = (43.933039, 15.444385) # location of unknown modem
```

# Utility functions


```python
def set_origin(gps_location):
    """
    Convert gps_location to local corrdinate 
    and set that as origin.
    :param gps_location: GPS location where the origin needs to be set as origin
    :returns: A tuple (origin in cartesian coordinates, origin in utm coordinates)
    """
    origin_utm = list(arlpy.geo.pos(gps_location))
    origin_cartesian = [j-i for i,j in zip(list(arlpy.geo.pos(gps_location)), list(arlpy.geo.pos(gps_location)))]
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
    coord_cartesian = [j-i for i,j in zip(origin_utm, list(coord_utm))]
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
    utm_coord_computed = [i+j for i, j in zip(set_origin(gps_origin)[1], coord_computed)]
    gps_coord_computed = arlpy.geo.latlong(utm_coord_computed, zone)
    return gps_coord_computed


def plot(gps_location):
    """
    Plots the GPS location on folium map.
    :param gps_location: list of tuples of gps locations
    :returns: map
    """
    m = folium.Map(location=list(gps_location[0]), zoom_start=20)
    folium.Marker(list(gps_location[0]), popup='<i> Node 1 </i>').add_to(m)
    folium.Marker(list(gps_location[1]), popup='<i> Node 2 </i>').add_to(m)
    if len(gps_location) == 3:
        folium.Marker(list(gps_location[2]), popup='<i> Node 3 </i>',\
                      icon=folium.Icon(color='red', icon='unknown')).add_to(m)
    else:
        folium.Marker(list(gps_location[2]), popup='<i> Node 3 </i>', \
                      icon=folium.Icon(color='red', icon='unknown')).add_to(m)
        folium.CircleMarker(list(gps_location[3]), popup='<i> Node3 C Localized </i>', \
                            fill=True, color = 'red').add_to(m)
    return m

def flush_modem(modem):
    """
    Clears the buffer in the modem.
    :param modem: A gateway connection handle to the modem
    """
    while modem.receive(timeout=1000):
        pass
```

# Computation of the local coordinates


```python
# Set Node 1 as origin
node1_coordinates = set_origin(gps_location[0])

# Convert Node 2 gps location to local coordinates
node2_coordinates = gps_to_local(gps_location[1], gps_location[0])

# Convert Node 3 gps location to local coordinates
node3_coordinates = gps_to_local(ground_truth, gps_location[0])
```

# Plot the locations on map


```python
# Three gps locations of the three nodes
locations = [gps_location[0], gps_location[1], ground_truth]

plot(locations)
```




<div style="width:100%;"><div style="position:relative;width:100%;height:0;padding-bottom:60%;"><iframe src="data:text/html;charset=utf-8;base64,PCFET0NUWVBFIGh0bWw+CjxoZWFkPiAgICAKICAgIDxtZXRhIGh0dHAtZXF1aXY9ImNvbnRlbnQtdHlwZSIgY29udGVudD0idGV4dC9odG1sOyBjaGFyc2V0PVVURi04IiAvPgogICAgPHNjcmlwdD5MX1BSRUZFUl9DQU5WQVM9ZmFsc2U7IExfTk9fVE9VQ0g9ZmFsc2U7IExfRElTQUJMRV8zRD1mYWxzZTs8L3NjcmlwdD4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL2xlYWZsZXRAMS4yLjAvZGlzdC9sZWFmbGV0LmpzIj48L3NjcmlwdD4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2FqYXguZ29vZ2xlYXBpcy5jb20vYWpheC9saWJzL2pxdWVyeS8xLjExLjEvanF1ZXJ5Lm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0IHNyYz0iaHR0cHM6Ly9tYXhjZG4uYm9vdHN0cmFwY2RuLmNvbS9ib290c3RyYXAvMy4yLjAvanMvYm9vdHN0cmFwLm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0IHNyYz0iaHR0cHM6Ly9jZG5qcy5jbG91ZGZsYXJlLmNvbS9hamF4L2xpYnMvTGVhZmxldC5hd2Vzb21lLW1hcmtlcnMvMi4wLjIvbGVhZmxldC5hd2Vzb21lLW1hcmtlcnMuanMiPjwvc2NyaXB0PgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL2xlYWZsZXRAMS4yLjAvZGlzdC9sZWFmbGV0LmNzcyIvPgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL21heGNkbi5ib290c3RyYXBjZG4uY29tL2Jvb3RzdHJhcC8zLjIuMC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MiLz4KICAgIDxsaW5rIHJlbD0ic3R5bGVzaGVldCIgaHJlZj0iaHR0cHM6Ly9tYXhjZG4uYm9vdHN0cmFwY2RuLmNvbS9ib290c3RyYXAvMy4yLjAvY3NzL2Jvb3RzdHJhcC10aGVtZS5taW4uY3NzIi8+CiAgICA8bGluayByZWw9InN0eWxlc2hlZXQiIGhyZWY9Imh0dHBzOi8vbWF4Y2RuLmJvb3RzdHJhcGNkbi5jb20vZm9udC1hd2Vzb21lLzQuNi4zL2Nzcy9mb250LWF3ZXNvbWUubWluLmNzcyIvPgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL2NkbmpzLmNsb3VkZmxhcmUuY29tL2FqYXgvbGlicy9MZWFmbGV0LmF3ZXNvbWUtbWFya2Vycy8yLjAuMi9sZWFmbGV0LmF3ZXNvbWUtbWFya2Vycy5jc3MiLz4KICAgIDxsaW5rIHJlbD0ic3R5bGVzaGVldCIgaHJlZj0iaHR0cHM6Ly9yYXdnaXQuY29tL3B5dGhvbi12aXN1YWxpemF0aW9uL2ZvbGl1bS9tYXN0ZXIvZm9saXVtL3RlbXBsYXRlcy9sZWFmbGV0LmF3ZXNvbWUucm90YXRlLmNzcyIvPgogICAgPHN0eWxlPmh0bWwsIGJvZHkge3dpZHRoOiAxMDAlO2hlaWdodDogMTAwJTttYXJnaW46IDA7cGFkZGluZzogMDt9PC9zdHlsZT4KICAgIDxzdHlsZT4jbWFwIHtwb3NpdGlvbjphYnNvbHV0ZTt0b3A6MDtib3R0b206MDtyaWdodDowO2xlZnQ6MDt9PC9zdHlsZT4KICAgIAogICAgPHN0eWxlPiNtYXBfNzkzNGIwZWMxYTcxNGI0YzhiNGQwYTViYTYwMTViOGYgewogICAgICAgIHBvc2l0aW9uOiByZWxhdGl2ZTsKICAgICAgICB3aWR0aDogMTAwLjAlOwogICAgICAgIGhlaWdodDogMTAwLjAlOwogICAgICAgIGxlZnQ6IDAuMCU7CiAgICAgICAgdG9wOiAwLjAlOwogICAgICAgIH0KICAgIDwvc3R5bGU+CjwvaGVhZD4KPGJvZHk+ICAgIAogICAgCiAgICA8ZGl2IGNsYXNzPSJmb2xpdW0tbWFwIiBpZD0ibWFwXzc5MzRiMGVjMWE3MTRiNGM4YjRkMGE1YmE2MDE1YjhmIiA+PC9kaXY+CjwvYm9keT4KPHNjcmlwdD4gICAgCiAgICAKICAgIAogICAgICAgIHZhciBib3VuZHMgPSBudWxsOwogICAgCgogICAgdmFyIG1hcF83OTM0YjBlYzFhNzE0YjRjOGI0ZDBhNWJhNjAxNWI4ZiA9IEwubWFwKAogICAgICAgICdtYXBfNzkzNGIwZWMxYTcxNGI0YzhiNGQwYTViYTYwMTViOGYnLCB7CiAgICAgICAgY2VudGVyOiBbNDMuOTMzMTMsIDE1LjQ0MzkxNV0sCiAgICAgICAgem9vbTogMjAsCiAgICAgICAgbWF4Qm91bmRzOiBib3VuZHMsCiAgICAgICAgbGF5ZXJzOiBbXSwKICAgICAgICB3b3JsZENvcHlKdW1wOiBmYWxzZSwKICAgICAgICBjcnM6IEwuQ1JTLkVQU0czODU3LAogICAgICAgIHpvb21Db250cm9sOiB0cnVlLAogICAgICAgIH0pOwoKICAgIAogICAgCiAgICB2YXIgdGlsZV9sYXllcl8zNDQ2MDY4ZWU3MDA0NmQyYTYwNTVlODY1ZGRhNmQ2NyA9IEwudGlsZUxheWVyKAogICAgICAgICdodHRwczovL3tzfS50aWxlLm9wZW5zdHJlZXRtYXAub3JnL3t6fS97eH0ve3l9LnBuZycsCiAgICAgICAgewogICAgICAgICJhdHRyaWJ1dGlvbiI6IG51bGwsCiAgICAgICAgImRldGVjdFJldGluYSI6IGZhbHNlLAogICAgICAgICJtYXhOYXRpdmVab29tIjogMTgsCiAgICAgICAgIm1heFpvb20iOiAxOCwKICAgICAgICAibWluWm9vbSI6IDAsCiAgICAgICAgIm5vV3JhcCI6IGZhbHNlLAogICAgICAgICJzdWJkb21haW5zIjogImFiYyIKfSkuYWRkVG8obWFwXzc5MzRiMGVjMWE3MTRiNGM4YjRkMGE1YmE2MDE1YjhmKTsKICAgIAogICAgICAgIHZhciBtYXJrZXJfY2U1MWFkMzI1YjgwNDJkNWI3MzdhNjFkODZjODM2M2EgPSBMLm1hcmtlcigKICAgICAgICAgICAgWzQzLjkzMzEzLCAxNS40NDM5MTVdLAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICBpY29uOiBuZXcgTC5JY29uLkRlZmF1bHQoKQogICAgICAgICAgICAgICAgfQogICAgICAgICAgICApLmFkZFRvKG1hcF83OTM0YjBlYzFhNzE0YjRjOGI0ZDBhNWJhNjAxNWI4Zik7CiAgICAgICAgCiAgICAKICAgICAgICAgICAgdmFyIHBvcHVwXzc2OWQ4YmM4NjAzNzQwYWVhYWUyZmQ2ODc0NjI4ZjE3ID0gTC5wb3B1cCh7bWF4V2lkdGg6ICczMDAnCiAgICAgICAgICAgIAogICAgICAgICAgICB9KTsKCiAgICAgICAgICAgIAogICAgICAgICAgICAgICAgdmFyIGh0bWxfNTI2NzNiOTgzZmFhNDUxNDgxODI2ZDk2MGFlODk2OWIgPSAkKCc8ZGl2IGlkPSJodG1sXzUyNjczYjk4M2ZhYTQ1MTQ4MTgyNmQ5NjBhZTg5NjliIiBzdHlsZT0id2lkdGg6IDEwMC4wJTsgaGVpZ2h0OiAxMDAuMCU7Ij48aT4gTm9kZSAxIDwvaT48L2Rpdj4nKVswXTsKICAgICAgICAgICAgICAgIHBvcHVwXzc2OWQ4YmM4NjAzNzQwYWVhYWUyZmQ2ODc0NjI4ZjE3LnNldENvbnRlbnQoaHRtbF81MjY3M2I5ODNmYWE0NTE0ODE4MjZkOTYwYWU4OTY5Yik7CiAgICAgICAgICAgIAoKICAgICAgICAgICAgbWFya2VyX2NlNTFhZDMyNWI4MDQyZDViNzM3YTYxZDg2YzgzNjNhLmJpbmRQb3B1cChwb3B1cF83NjlkOGJjODYwMzc0MGFlYWFlMmZkNjg3NDYyOGYxNykKICAgICAgICAgICAgOwoKICAgICAgICAgICAgCiAgICAgICAgCiAgICAKICAgICAgICB2YXIgbWFya2VyX2ExNzJiNzhlZTE1MDQ5NzhiMjFiZjIyMWY5NmE4MGM1ID0gTC5tYXJrZXIoCiAgICAgICAgICAgIFs0My45MzMyMzUsIDE1LjQ0NDA3MV0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgIGljb246IG5ldyBMLkljb24uRGVmYXVsdCgpCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICkuYWRkVG8obWFwXzc5MzRiMGVjMWE3MTRiNGM4YjRkMGE1YmE2MDE1YjhmKTsKICAgICAgICAKICAgIAogICAgICAgICAgICB2YXIgcG9wdXBfM2QyMmUyMWFiM2E4NGQxNzkzOGNkOGJlNGZmN2MyMTQgPSBMLnBvcHVwKHttYXhXaWR0aDogJzMwMCcKICAgICAgICAgICAgCiAgICAgICAgICAgIH0pOwoKICAgICAgICAgICAgCiAgICAgICAgICAgICAgICB2YXIgaHRtbF8zMDcyNjBjN2FlNTQ0ZTY1OTQ0ZDcwZTY0YWMwN2MxNCA9ICQoJzxkaXYgaWQ9Imh0bWxfMzA3MjYwYzdhZTU0NGU2NTk0NGQ3MGU2NGFjMDdjMTQiIHN0eWxlPSJ3aWR0aDogMTAwLjAlOyBoZWlnaHQ6IDEwMC4wJTsiPjxpPiBOb2RlIDIgPC9pPjwvZGl2PicpWzBdOwogICAgICAgICAgICAgICAgcG9wdXBfM2QyMmUyMWFiM2E4NGQxNzkzOGNkOGJlNGZmN2MyMTQuc2V0Q29udGVudChodG1sXzMwNzI2MGM3YWU1NDRlNjU5NDRkNzBlNjRhYzA3YzE0KTsKICAgICAgICAgICAgCgogICAgICAgICAgICBtYXJrZXJfYTE3MmI3OGVlMTUwNDk3OGIyMWJmMjIxZjk2YTgwYzUuYmluZFBvcHVwKHBvcHVwXzNkMjJlMjFhYjNhODRkMTc5MzhjZDhiZTRmZjdjMjE0KQogICAgICAgICAgICA7CgogICAgICAgICAgICAKICAgICAgICAKICAgIAogICAgICAgIHZhciBtYXJrZXJfMjMxZjY0NzllNDMyNDQ5NzlkZDQ1YmRjOGUyNjQzYTkgPSBMLm1hcmtlcigKICAgICAgICAgICAgWzQzLjkzMzAzOSwgMTUuNDQ0Mzg1XSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAgaWNvbjogbmV3IEwuSWNvbi5EZWZhdWx0KCkKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgKS5hZGRUbyhtYXBfNzkzNGIwZWMxYTcxNGI0YzhiNGQwYTViYTYwMTViOGYpOwogICAgICAgIAogICAgCgogICAgICAgICAgICAgICAgdmFyIGljb25fZmVhNDVlNzQ5YTgzNDZkYzhhOTUzZmNlNTgzYmMzODkgPSBMLkF3ZXNvbWVNYXJrZXJzLmljb24oewogICAgICAgICAgICAgICAgICAgIGljb246ICd1bmtub3duJywKICAgICAgICAgICAgICAgICAgICBpY29uQ29sb3I6ICd3aGl0ZScsCiAgICAgICAgICAgICAgICAgICAgbWFya2VyQ29sb3I6ICdyZWQnLAogICAgICAgICAgICAgICAgICAgIHByZWZpeDogJ2dseXBoaWNvbicsCiAgICAgICAgICAgICAgICAgICAgZXh0cmFDbGFzc2VzOiAnZmEtcm90YXRlLTAnCiAgICAgICAgICAgICAgICAgICAgfSk7CiAgICAgICAgICAgICAgICBtYXJrZXJfMjMxZjY0NzllNDMyNDQ5NzlkZDQ1YmRjOGUyNjQzYTkuc2V0SWNvbihpY29uX2ZlYTQ1ZTc0OWE4MzQ2ZGM4YTk1M2ZjZTU4M2JjMzg5KTsKICAgICAgICAgICAgCiAgICAKICAgICAgICAgICAgdmFyIHBvcHVwX2I2NGI1ZWZjZDc5YTRkYjI4NzJjZGRlYTI1N2U0YTdmID0gTC5wb3B1cCh7bWF4V2lkdGg6ICczMDAnCiAgICAgICAgICAgIAogICAgICAgICAgICB9KTsKCiAgICAgICAgICAgIAogICAgICAgICAgICAgICAgdmFyIGh0bWxfMDhiZGIxYTFkMmQyNDE3NTlkN2IzNWY5MzMxOTMxYWMgPSAkKCc8ZGl2IGlkPSJodG1sXzA4YmRiMWExZDJkMjQxNzU5ZDdiMzVmOTMzMTkzMWFjIiBzdHlsZT0id2lkdGg6IDEwMC4wJTsgaGVpZ2h0OiAxMDAuMCU7Ij48aT4gTm9kZSAzIDwvaT48L2Rpdj4nKVswXTsKICAgICAgICAgICAgICAgIHBvcHVwX2I2NGI1ZWZjZDc5YTRkYjI4NzJjZGRlYTI1N2U0YTdmLnNldENvbnRlbnQoaHRtbF8wOGJkYjFhMWQyZDI0MTc1OWQ3YjM1ZjkzMzE5MzFhYyk7CiAgICAgICAgICAgIAoKICAgICAgICAgICAgbWFya2VyXzIzMWY2NDc5ZTQzMjQ0OTc5ZGQ0NWJkYzhlMjY0M2E5LmJpbmRQb3B1cChwb3B1cF9iNjRiNWVmY2Q3OWE0ZGIyODcyY2RkZWEyNTdlNGE3ZikKICAgICAgICAgICAgOwoKICAgICAgICAgICAgCiAgICAgICAgCjwvc2NyaXB0Pg==" style="position:absolute;width:100%;height:100%;left:0;top:0;border:none !important;" allowfullscreen webkitallowfullscreen mozallowfullscreen></iframe></div></div>



# Open connection to the modems


```python
# Connection to Node 1
node1gw = UnetGateway(ip_address[0], 1101)
node1 = node1gw.agentForService(Services.NODE_INFO)
```


```python
# Connection to Node 2
node2gw = UnetGateway(ip_address[1], 1102)
node2 = node2gw.agentForService(Services.NODE_INFO)
```


```python
node1
```




    
    [org.arl.unet.nodeinfo.NodeInfoParam]
      address = 1
      canForward = False
      diveRate = 0.0
      heading = 0.0
      location = [0.0, 0.0, 0.0]
      mobility = False
      nodeName = 1
      origin = []
      speed = 0.0
      time = Sep 30, 2018 1:11:30 PM
      turnRate = 0.0





```python
node1.location
```




    [0.0, 0.0, 0.0]




```python
node2
```




    
    [org.arl.unet.nodeinfo.NodeInfoParam]
      address = 2
      canForward = False
      diveRate = 0.0
      heading = 0.0
      location = [12.45850165025331, 11.72931706160307, 0.0]
      mobility = False
      nodeName = 2
      origin = []
      speed = 0.0
      time = Sep 30, 2018 1:11:32 PM
      turnRate = 0.0





```python
node2.location 
```




    [12.45850165025331, 11.72931706160307, 0.0]



Now, that the network simulator is setup with two known locations of the modem in the local coordinates system, we set out to compute the GPS location of the thrid node. We need to measure the distance from the two known locations to the modem we are trying to locate. This can be achived using ranging funtionality in UnetStack.

# Ranging to measure distances


```python
ranging_node1 = node1gw.agentForService(Services.RANGING)
node1gw.subscribe(ranging_node1)
```


```python
ranging_node2 = node2gw.agentForService(Services.RANGING)
node2gw.subscribe(ranging_node2)
```


```python
ranging_node1 << org_arl_unet_phy.RangeReq(to=3)
```




    AGREE




```python
rnf1 = node1gw.receive(RangeNtf, 5000)
if rnf1 is not None:
    range1 = rnf1.getRange() 
    print('The range from Node 1 is : ' + str(range1) + ' m.')
else:
    print('Range not measured, try again.')
```

    The range from Node 1 is : 39.04 m.



```python
flush_modem(node1gw)
```


```python
ranging_node2 << org_arl_unet_phy.RangeReq(to=3)
```




    AGREE




```python
rnf2 = node2gw.receive(RangeNtf, 5000)
if rnf2 is not None:
    range2 = rnf2.getRange() 
    print('The range from Node 2 is : ' + str(range2) + ' m.')
else:
    print('Range not measured, try again.')
```

    The range from Node 2 is : 33.29 m.



```python
flush_modem(node2gw)
```

The distances are measured using acoustic ranging as shown above.

# Localization algorithm

Let us denote the unknown modem's location as $(x_1, x_2)$. The known position of the modemA is $(a_1, a_2)$ and modemB is $(b_1, b_2)$. The measured distances are denoted by $r_1$ and $r_2$.

We use the symbolic manipulation toolbox `sympy` in python to compute the analytic expression for computing $(x_1, x_2)$. The details are as given below:


```python
x1, x2, a1, a2, b1, b2, r1, r2 = symbols('x1 x2 a1 a2 b1 b2 r1 r2')
```

Write the variable $x_1$ in terms of all other known parameters and variable $x_2$


```python
x1 = ((a1**2 - b1**2) + (a2**2 - b2**2) - (r1**2 - r2**2) -2*x2*(a2-b2))/(2*(a1-b1))
```

Compute the expression for $x_2$ symbolically


```python
expr = solveset(Eq((x1 - a1)**2 + (x2 - a2)**2 - r1**2, 0), x2)
```

Now we know the expression for $x_2$ in terms of all known parameters. Therefore, it's value can be computed using substitution as shown below:


```python
x2_sol = list(expr.subs([(a1, node1.location[0]), (a2, node1.location[1]), \
           (b1, node2.location[0]), (b2, node2.location[1]), \
           (r1, range1), (r2, range2)]).evalf())
```

There are two possible solutions. The decision is based on the value of $x_1$, since our modem which is being localized lies on the right side of the y-axis, we take the decision based on the sign of $x_1$.


```python
x2_sol
```




    [-9.90113106055126, 38.2914495705714]



The value of $x_1$ is computed for both values of computed $x_2$.


```python
x1_sol = []
x1_sol.append( x1.subs([(a1, node1.location[0]), (a2, node1.location[1]), \
               (b1, node2.location[0]), (b2, node2.location[1]), \
               (r1, range1), (r2, range2), \
               (x2, x2_sol[0])]).evalf() ) 
x1_sol.append( x1.subs([(a1, node1.location[0]), (a2, node1.location[1]), \
               (b1, node2.location[0]), (b2, node2.location[1]), \
               (r1, range1), (r2, range2), \
               (x2, x2_sol[1])]).evalf() ) 
```


```python
x1_sol 
```




    [37.7635962763319, -7.60831714536076]




```python
slope = (node1.location[1]-node2.location[1])/(node1.location[0]-node2.location[0])
```


```python
slope
```




    0.9414709241030268




```python
if slope > 0:
    # check if the node1 x coordinate is less than the computed x-coordinate
    if x1_sol[0] > node1.location[0]:
        x1 = x1_sol[0]
        x2 = x2_sol[0]
    else:
        x1 = x1_sol[1]
        x2 = x2_sol[1]
else:
    # check if the node2 x coordinate is less than the computed x-coordinate
    if x1_sol[0] > node2.location[0]:
        x1 = x1_sol[0]
        x2 = x2_sol[0]
    else:
        x1 = x1_sol[1]
        x2 = x2_sol[1]        
```


```python
print(x1, x2)
```

    37.7635962763319 -9.90113106055126



```python
index = [idx for idx, val in enumerate(x1_sol) if val >= 0][0]
x1 = x1_sol[index]
x2 = x2_sol[index]
print((x1, x2, 0.0))
```

# Convert the computed local coordinate to GPS coordinate


```python
coord_cartesian = (x1, x2, 0.0)
gps_coordinates_computed = local_to_gps(coord_cartesian, gps_location[0])
```


```python
# Three gps locations of the three nodes
locations = [gps_location[0], gps_location[1], ground_truth, gps_coordinates_computed]

plot(locations)
```




<div style="width:100%;"><div style="position:relative;width:100%;height:0;padding-bottom:60%;"><iframe src="data:text/html;charset=utf-8;base64,PCFET0NUWVBFIGh0bWw+CjxoZWFkPiAgICAKICAgIDxtZXRhIGh0dHAtZXF1aXY9ImNvbnRlbnQtdHlwZSIgY29udGVudD0idGV4dC9odG1sOyBjaGFyc2V0PVVURi04IiAvPgogICAgPHNjcmlwdD5MX1BSRUZFUl9DQU5WQVM9ZmFsc2U7IExfTk9fVE9VQ0g9ZmFsc2U7IExfRElTQUJMRV8zRD1mYWxzZTs8L3NjcmlwdD4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL2xlYWZsZXRAMS4yLjAvZGlzdC9sZWFmbGV0LmpzIj48L3NjcmlwdD4KICAgIDxzY3JpcHQgc3JjPSJodHRwczovL2FqYXguZ29vZ2xlYXBpcy5jb20vYWpheC9saWJzL2pxdWVyeS8xLjExLjEvanF1ZXJ5Lm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0IHNyYz0iaHR0cHM6Ly9tYXhjZG4uYm9vdHN0cmFwY2RuLmNvbS9ib290c3RyYXAvMy4yLjAvanMvYm9vdHN0cmFwLm1pbi5qcyI+PC9zY3JpcHQ+CiAgICA8c2NyaXB0IHNyYz0iaHR0cHM6Ly9jZG5qcy5jbG91ZGZsYXJlLmNvbS9hamF4L2xpYnMvTGVhZmxldC5hd2Vzb21lLW1hcmtlcnMvMi4wLjIvbGVhZmxldC5hd2Vzb21lLW1hcmtlcnMuanMiPjwvc2NyaXB0PgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL2xlYWZsZXRAMS4yLjAvZGlzdC9sZWFmbGV0LmNzcyIvPgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL21heGNkbi5ib290c3RyYXBjZG4uY29tL2Jvb3RzdHJhcC8zLjIuMC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MiLz4KICAgIDxsaW5rIHJlbD0ic3R5bGVzaGVldCIgaHJlZj0iaHR0cHM6Ly9tYXhjZG4uYm9vdHN0cmFwY2RuLmNvbS9ib290c3RyYXAvMy4yLjAvY3NzL2Jvb3RzdHJhcC10aGVtZS5taW4uY3NzIi8+CiAgICA8bGluayByZWw9InN0eWxlc2hlZXQiIGhyZWY9Imh0dHBzOi8vbWF4Y2RuLmJvb3RzdHJhcGNkbi5jb20vZm9udC1hd2Vzb21lLzQuNi4zL2Nzcy9mb250LWF3ZXNvbWUubWluLmNzcyIvPgogICAgPGxpbmsgcmVsPSJzdHlsZXNoZWV0IiBocmVmPSJodHRwczovL2NkbmpzLmNsb3VkZmxhcmUuY29tL2FqYXgvbGlicy9MZWFmbGV0LmF3ZXNvbWUtbWFya2Vycy8yLjAuMi9sZWFmbGV0LmF3ZXNvbWUtbWFya2Vycy5jc3MiLz4KICAgIDxsaW5rIHJlbD0ic3R5bGVzaGVldCIgaHJlZj0iaHR0cHM6Ly9yYXdnaXQuY29tL3B5dGhvbi12aXN1YWxpemF0aW9uL2ZvbGl1bS9tYXN0ZXIvZm9saXVtL3RlbXBsYXRlcy9sZWFmbGV0LmF3ZXNvbWUucm90YXRlLmNzcyIvPgogICAgPHN0eWxlPmh0bWwsIGJvZHkge3dpZHRoOiAxMDAlO2hlaWdodDogMTAwJTttYXJnaW46IDA7cGFkZGluZzogMDt9PC9zdHlsZT4KICAgIDxzdHlsZT4jbWFwIHtwb3NpdGlvbjphYnNvbHV0ZTt0b3A6MDtib3R0b206MDtyaWdodDowO2xlZnQ6MDt9PC9zdHlsZT4KICAgIAogICAgPHN0eWxlPiNtYXBfYjBlMjUxNzcyOGE1NDUyNmFjOWIyMmRhYjgzODhiMmMgewogICAgICAgIHBvc2l0aW9uOiByZWxhdGl2ZTsKICAgICAgICB3aWR0aDogMTAwLjAlOwogICAgICAgIGhlaWdodDogMTAwLjAlOwogICAgICAgIGxlZnQ6IDAuMCU7CiAgICAgICAgdG9wOiAwLjAlOwogICAgICAgIH0KICAgIDwvc3R5bGU+CjwvaGVhZD4KPGJvZHk+ICAgIAogICAgCiAgICA8ZGl2IGNsYXNzPSJmb2xpdW0tbWFwIiBpZD0ibWFwX2IwZTI1MTc3MjhhNTQ1MjZhYzliMjJkYWI4Mzg4YjJjIiA+PC9kaXY+CjwvYm9keT4KPHNjcmlwdD4gICAgCiAgICAKICAgIAogICAgICAgIHZhciBib3VuZHMgPSBudWxsOwogICAgCgogICAgdmFyIG1hcF9iMGUyNTE3NzI4YTU0NTI2YWM5YjIyZGFiODM4OGIyYyA9IEwubWFwKAogICAgICAgICdtYXBfYjBlMjUxNzcyOGE1NDUyNmFjOWIyMmRhYjgzODhiMmMnLCB7CiAgICAgICAgY2VudGVyOiBbNDMuOTMzMTMsIDE1LjQ0MzkxNV0sCiAgICAgICAgem9vbTogMjAsCiAgICAgICAgbWF4Qm91bmRzOiBib3VuZHMsCiAgICAgICAgbGF5ZXJzOiBbXSwKICAgICAgICB3b3JsZENvcHlKdW1wOiBmYWxzZSwKICAgICAgICBjcnM6IEwuQ1JTLkVQU0czODU3LAogICAgICAgIHpvb21Db250cm9sOiB0cnVlLAogICAgICAgIH0pOwoKICAgIAogICAgCiAgICB2YXIgdGlsZV9sYXllcl80N2Q4ZjdkNjBmNWY0ZTRkOTcxYTYwMWRhNGM3MDUwNSA9IEwudGlsZUxheWVyKAogICAgICAgICdodHRwczovL3tzfS50aWxlLm9wZW5zdHJlZXRtYXAub3JnL3t6fS97eH0ve3l9LnBuZycsCiAgICAgICAgewogICAgICAgICJhdHRyaWJ1dGlvbiI6IG51bGwsCiAgICAgICAgImRldGVjdFJldGluYSI6IGZhbHNlLAogICAgICAgICJtYXhOYXRpdmVab29tIjogMTgsCiAgICAgICAgIm1heFpvb20iOiAxOCwKICAgICAgICAibWluWm9vbSI6IDAsCiAgICAgICAgIm5vV3JhcCI6IGZhbHNlLAogICAgICAgICJzdWJkb21haW5zIjogImFiYyIKfSkuYWRkVG8obWFwX2IwZTI1MTc3MjhhNTQ1MjZhYzliMjJkYWI4Mzg4YjJjKTsKICAgIAogICAgICAgIHZhciBtYXJrZXJfZTEzNGIzZTdlYjk4NGE0YTljYjQ4ZTdiNTRjZGVjZmMgPSBMLm1hcmtlcigKICAgICAgICAgICAgWzQzLjkzMzEzLCAxNS40NDM5MTVdLAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICBpY29uOiBuZXcgTC5JY29uLkRlZmF1bHQoKQogICAgICAgICAgICAgICAgfQogICAgICAgICAgICApLmFkZFRvKG1hcF9iMGUyNTE3NzI4YTU0NTI2YWM5YjIyZGFiODM4OGIyYyk7CiAgICAgICAgCiAgICAKICAgICAgICAgICAgdmFyIHBvcHVwXzdhNTA4OTBhZTY0ZDRhMDE5OTEyY2Y2NGRjZmE4ZGNiID0gTC5wb3B1cCh7bWF4V2lkdGg6ICczMDAnCiAgICAgICAgICAgIAogICAgICAgICAgICB9KTsKCiAgICAgICAgICAgIAogICAgICAgICAgICAgICAgdmFyIGh0bWxfMjc2OWFlNjRmNTZhNGI2YjlkNDAzYzQ4Yzc5YjkzZmYgPSAkKCc8ZGl2IGlkPSJodG1sXzI3NjlhZTY0ZjU2YTRiNmI5ZDQwM2M0OGM3OWI5M2ZmIiBzdHlsZT0id2lkdGg6IDEwMC4wJTsgaGVpZ2h0OiAxMDAuMCU7Ij48aT4gTm9kZSAxIDwvaT48L2Rpdj4nKVswXTsKICAgICAgICAgICAgICAgIHBvcHVwXzdhNTA4OTBhZTY0ZDRhMDE5OTEyY2Y2NGRjZmE4ZGNiLnNldENvbnRlbnQoaHRtbF8yNzY5YWU2NGY1NmE0YjZiOWQ0MDNjNDhjNzliOTNmZik7CiAgICAgICAgICAgIAoKICAgICAgICAgICAgbWFya2VyX2UxMzRiM2U3ZWI5ODRhNGE5Y2I0OGU3YjU0Y2RlY2ZjLmJpbmRQb3B1cChwb3B1cF83YTUwODkwYWU2NGQ0YTAxOTkxMmNmNjRkY2ZhOGRjYikKICAgICAgICAgICAgOwoKICAgICAgICAgICAgCiAgICAgICAgCiAgICAKICAgICAgICB2YXIgbWFya2VyXzEwZTc2NTVmNDE2ODQ5OTM5YThkNmE4YmE2NTIxMWEwID0gTC5tYXJrZXIoCiAgICAgICAgICAgIFs0My45MzMyMzUsIDE1LjQ0NDA3MV0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgIGljb246IG5ldyBMLkljb24uRGVmYXVsdCgpCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICkuYWRkVG8obWFwX2IwZTI1MTc3MjhhNTQ1MjZhYzliMjJkYWI4Mzg4YjJjKTsKICAgICAgICAKICAgIAogICAgICAgICAgICB2YXIgcG9wdXBfMjk0YmUzM2NiNDk5NDI3ZDk4Y2M1NjNmODQxNTk5MDIgPSBMLnBvcHVwKHttYXhXaWR0aDogJzMwMCcKICAgICAgICAgICAgCiAgICAgICAgICAgIH0pOwoKICAgICAgICAgICAgCiAgICAgICAgICAgICAgICB2YXIgaHRtbF84OTEzMjYyZDkyMzk0MGRmOGZjZTYyYzM3YmRlN2JjMyA9ICQoJzxkaXYgaWQ9Imh0bWxfODkxMzI2MmQ5MjM5NDBkZjhmY2U2MmMzN2JkZTdiYzMiIHN0eWxlPSJ3aWR0aDogMTAwLjAlOyBoZWlnaHQ6IDEwMC4wJTsiPjxpPiBOb2RlIDIgPC9pPjwvZGl2PicpWzBdOwogICAgICAgICAgICAgICAgcG9wdXBfMjk0YmUzM2NiNDk5NDI3ZDk4Y2M1NjNmODQxNTk5MDIuc2V0Q29udGVudChodG1sXzg5MTMyNjJkOTIzOTQwZGY4ZmNlNjJjMzdiZGU3YmMzKTsKICAgICAgICAgICAgCgogICAgICAgICAgICBtYXJrZXJfMTBlNzY1NWY0MTY4NDk5MzlhOGQ2YThiYTY1MjExYTAuYmluZFBvcHVwKHBvcHVwXzI5NGJlMzNjYjQ5OTQyN2Q5OGNjNTYzZjg0MTU5OTAyKQogICAgICAgICAgICA7CgogICAgICAgICAgICAKICAgICAgICAKICAgIAogICAgICAgIHZhciBtYXJrZXJfNzkyMGYwZmQzNGY5NDhjYTg5MjA4YjQyYmNjMWQxMjIgPSBMLm1hcmtlcigKICAgICAgICAgICAgWzQzLjkzMzAzOSwgMTUuNDQ0Mzg1XSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAgaWNvbjogbmV3IEwuSWNvbi5EZWZhdWx0KCkKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgKS5hZGRUbyhtYXBfYjBlMjUxNzcyOGE1NDUyNmFjOWIyMmRhYjgzODhiMmMpOwogICAgICAgIAogICAgCgogICAgICAgICAgICAgICAgdmFyIGljb25fMTA4MjBjOWM1OGRhNGY5MmJkNGZhZWRhMDg2OTk5YmYgPSBMLkF3ZXNvbWVNYXJrZXJzLmljb24oewogICAgICAgICAgICAgICAgICAgIGljb246ICd1bmtub3duJywKICAgICAgICAgICAgICAgICAgICBpY29uQ29sb3I6ICd3aGl0ZScsCiAgICAgICAgICAgICAgICAgICAgbWFya2VyQ29sb3I6ICdyZWQnLAogICAgICAgICAgICAgICAgICAgIHByZWZpeDogJ2dseXBoaWNvbicsCiAgICAgICAgICAgICAgICAgICAgZXh0cmFDbGFzc2VzOiAnZmEtcm90YXRlLTAnCiAgICAgICAgICAgICAgICAgICAgfSk7CiAgICAgICAgICAgICAgICBtYXJrZXJfNzkyMGYwZmQzNGY5NDhjYTg5MjA4YjQyYmNjMWQxMjIuc2V0SWNvbihpY29uXzEwODIwYzljNThkYTRmOTJiZDRmYWVkYTA4Njk5OWJmKTsKICAgICAgICAgICAgCiAgICAKICAgICAgICAgICAgdmFyIHBvcHVwXzZiN2Q0ZTYyNjg3MTRlM2JhNWFmZjdiMDYzZTBjOThjID0gTC5wb3B1cCh7bWF4V2lkdGg6ICczMDAnCiAgICAgICAgICAgIAogICAgICAgICAgICB9KTsKCiAgICAgICAgICAgIAogICAgICAgICAgICAgICAgdmFyIGh0bWxfYjJmYzg1YTk3OTExNGYwNWI4NjVjZWIzMzkwN2EyODYgPSAkKCc8ZGl2IGlkPSJodG1sX2IyZmM4NWE5NzkxMTRmMDViODY1Y2ViMzM5MDdhMjg2IiBzdHlsZT0id2lkdGg6IDEwMC4wJTsgaGVpZ2h0OiAxMDAuMCU7Ij48aT4gTm9kZSAzIDwvaT48L2Rpdj4nKVswXTsKICAgICAgICAgICAgICAgIHBvcHVwXzZiN2Q0ZTYyNjg3MTRlM2JhNWFmZjdiMDYzZTBjOThjLnNldENvbnRlbnQoaHRtbF9iMmZjODVhOTc5MTE0ZjA1Yjg2NWNlYjMzOTA3YTI4Nik7CiAgICAgICAgICAgIAoKICAgICAgICAgICAgbWFya2VyXzc5MjBmMGZkMzRmOTQ4Y2E4OTIwOGI0MmJjYzFkMTIyLmJpbmRQb3B1cChwb3B1cF82YjdkNGU2MjY4NzE0ZTNiYTVhZmY3YjA2M2UwYzk4YykKICAgICAgICAgICAgOwoKICAgICAgICAgICAgCiAgICAgICAgCiAgICAKICAgICAgICAgICAgdmFyIGNpcmNsZV9tYXJrZXJfYjJlYjFlODZhNjNmNDI4MjkzMmUyNGUwMjQwMzU2NzUgPSBMLmNpcmNsZU1hcmtlcigKICAgICAgICAgICAgICAgIFs0My45MzMwMzkwMjkwNTQzNTYsIDE1LjQ0NDM4NDgxOTgyNTI5N10sCiAgICAgICAgICAgICAgICB7CiAgImJ1YmJsaW5nTW91c2VFdmVudHMiOiB0cnVlLAogICJjb2xvciI6ICJyZWQiLAogICJkYXNoQXJyYXkiOiBudWxsLAogICJkYXNoT2Zmc2V0IjogbnVsbCwKICAiZmlsbCI6IHRydWUsCiAgImZpbGxDb2xvciI6ICJyZWQiLAogICJmaWxsT3BhY2l0eSI6IDAuMiwKICAiZmlsbFJ1bGUiOiAiZXZlbm9kZCIsCiAgImxpbmVDYXAiOiAicm91bmQiLAogICJsaW5lSm9pbiI6ICJyb3VuZCIsCiAgIm9wYWNpdHkiOiAxLjAsCiAgInJhZGl1cyI6IDEwLAogICJzdHJva2UiOiB0cnVlLAogICJ3ZWlnaHQiOiAzCn0KICAgICAgICAgICAgICAgICkKICAgICAgICAgICAgICAgIC5hZGRUbyhtYXBfYjBlMjUxNzcyOGE1NDUyNmFjOWIyMmRhYjgzODhiMmMpOwogICAgICAgICAgICAKICAgIAogICAgICAgICAgICB2YXIgcG9wdXBfYWYwMTg3ZDBjZGQxNDVhNDg2MjYzNGM5NzU0MGU2MmUgPSBMLnBvcHVwKHttYXhXaWR0aDogJzMwMCcKICAgICAgICAgICAgCiAgICAgICAgICAgIH0pOwoKICAgICAgICAgICAgCiAgICAgICAgICAgICAgICB2YXIgaHRtbF9lNWU3MDk3ZmVlNzk0MzcwOGY5ODY1Y2U1ZjRjMmRkOCA9ICQoJzxkaXYgaWQ9Imh0bWxfZTVlNzA5N2ZlZTc5NDM3MDhmOTg2NWNlNWY0YzJkZDgiIHN0eWxlPSJ3aWR0aDogMTAwLjAlOyBoZWlnaHQ6IDEwMC4wJTsiPjxpPiBOb2RlMyBDIExvY2FsaXplZCA8L2k+PC9kaXY+JylbMF07CiAgICAgICAgICAgICAgICBwb3B1cF9hZjAxODdkMGNkZDE0NWE0ODYyNjM0Yzk3NTQwZTYyZS5zZXRDb250ZW50KGh0bWxfZTVlNzA5N2ZlZTc5NDM3MDhmOTg2NWNlNWY0YzJkZDgpOwogICAgICAgICAgICAKCiAgICAgICAgICAgIGNpcmNsZV9tYXJrZXJfYjJlYjFlODZhNjNmNDI4MjkzMmUyNGUwMjQwMzU2NzUuYmluZFBvcHVwKHBvcHVwX2FmMDE4N2QwY2RkMTQ1YTQ4NjI2MzRjOTc1NDBlNjJlKQogICAgICAgICAgICA7CgogICAgICAgICAgICAKICAgICAgICAKPC9zY3JpcHQ+" style="position:absolute;width:100%;height:100%;left:0;top:0;border:none !important;" allowfullscreen webkitallowfullscreen mozallowfullscreen></iframe></div></div>



The map is updated with the localized node's location with a red circle as shown in the figure above.
