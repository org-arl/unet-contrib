import java.awt.Color
import org.arl.unet.utils.GPlot

a = []
x = []
y = []
Mission2012Channel.nodeLocation.each { addr, loc ->
  a << (addr as String)
  x << (loc[0] as double)
  y << (loc[1] as double)
}
new GPlot('MISSION 2012', 600, 600).with {
  xlabel('meters')
  ylabel('meters')
  plot('Nodes', x as double[], y as double[], Color.blue, true, false)
  a.eachWithIndex { s, ndx ->
    text(x[ndx], y[ndx]+20, s)
  }
  drawnow()
}
