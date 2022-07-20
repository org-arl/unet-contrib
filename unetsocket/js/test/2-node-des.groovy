import org.arl.fjage.*
import org.arl.fjage.param.*
import org.arl.unet.*

public enum DummyParam implements Parameter {
  param1,
  param2,
  param3
}

platform = RealTimePlatform   // use real-time mode

simulate {
  node 'A', location: [ 0.km, 0.km, -15.m], web: 8081, api: 1101, stack: "$home/etc/setup"
  node 'B', location: [ 1.km, 0.km, -15.m], web: 8082, api: 1102, stack: "$home/etc/setup"
  node 'C', location: [ 0.km, 1.km, -15.m], web: 8083, api: 1103, stack: { container ->
    container.add 'dummy', new UnetAgent(){
      private static final long TICK_RATE = 1000;
      int param1 = 0;
      int param2 = 0;
      int param3 = 0;

      @Override
      protected void startup() {
        super.startup()
        add new TickerBehavior(TICK_RATE, {
          param1++;
          param2++;
          ParamChangeNtf ntf = new ParamChangeNtf(topic(org.arl.unet.Topics.PARAMCHANGE));
          ntf.set(DummyParam.param1, this.param1);
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