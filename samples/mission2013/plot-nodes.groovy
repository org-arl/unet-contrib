import java.awt.Color
import org.arl.unet.utils.GPlot
import org.arl.unet.sim.channels.Mission2013a

a = []
x = []
y = []
Mission2013a.nodeLocation.each { addr, loc ->
  a << (addr as String)
  x << (loc[0] as double)
  y << (loc[1] as double)
}
new GPlot('MISSION 2013', 600, 600).with {
  xlabel('meters')
  ylabel('meters')
  plot('Nodes', x as double[], y as double[], Color.blue, true, false)
  a.eachWithIndex { s, ndx ->
    text(x[ndx], y[ndx]+20, s)
  }
  drawnow()
}
