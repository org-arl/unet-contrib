import org.arl.unet.utils.GPlot
import java.awt.Color

// read trace.nam to get simulation results
x = []
y = []
new File('logs/trace.nam').eachLine { s ->
  if (s.contains('STATS:')) {
    x << ((s =~ /O=([0-9\.]+)/)[0][1] as double)
    y << ((s =~ /T=([0-9\.]+)/)[0][1] as double)
  }
}

// plot the results against theory
new GPlot('Aloha Performance', 800, 600).with {
  xlabel('Offered Load')
  ylabel('Normalized Throughput')
  def xrange = range(0, 2.0, 0.05)
  plot('Pure Aloha', xrange, { x -> x*Math.exp(-2*x) })
  plot('Slotted Aloha', xrange, { x -> x*Math.exp(-x) })
  plot('Simulation', x as double[], y as double[], Color.red, true, false)
  drawnow()
}
