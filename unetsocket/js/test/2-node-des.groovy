import org.arl.fjage.*
import org.arl.fjage.param.*
import org.arl.unet.*

public enum DummyParam implements Parameter {
  dummyparam
}

platform = RealTimePlatform   // use real-time mode

simulate {
  node 'A', location: [ 0.km, 0.km, -15.m], web: 8081, api: 1101, stack: "$home/etc/setup"
  node 'B', location: [ 1.km, 0.km, -15.m], web: 8082, api: 1102, stack: { container ->
    container.add 'myagent', new UnetAgent(){
      private static final long TICK_RATE = 10000;
      private int dummyparam = 0;
      private AgentID notify = null;

      @Override
      protected void setup() {
        notify = topic();
      }

      @Override
      protected void startup() {
        super.startup()
        add new TickerBehavior(TICK_RATE, {
          log.info "dummyparam = ${dummyparam}"
          dummyparam++;
          ParamChangeNtf ntf = new ParamChangeNtf(notify);
          ntf.set(DummyParam.dummyparam, this.dummyparam);
          send(ntf);
        })
      }

      @Override
      List<Parameter> getParameterList() {
        return allOf(DummyParam)                            // advertise the list of parameters
      }
    }
  }
}