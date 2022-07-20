import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.ParamChangeNtf
import org.arl.unet.UnetAgent

public enum DummyParam implements Parameter {
  param1,
  param2,
  param3,
  enable
}

simulate {
  node 'A', location: [ 0.km, 0.km, -15.m], web: 8081, api: 1101, stack: "$home/etc/setup"
  def n = node 'B', location: [ 1.km, 0.km, -15.m], web: 8082, api: 1102, stack: "$home/etc/setup"
  n.container.add 'dummy', new UnetAgent() {
    private static final long TICK_RATE = 1000;
    long param1 = 0
    long param2 = 0
    long param3 = 0
    boolean enable = false;

    @Override
    protected void startup() {
      super.startup()
      add new TickerBehavior(TICK_RATE, {
        param1++;
        param2++;
        if (enable){
          ParamChangeNtf ntf = new ParamChangeNtf(topic(org.arl.unet.Topics.PARAMCHANGE));
          ntf.set(DummyParam.param1, this.param1);
          send(ntf);
        }
      })
    }

    @Override
    List<Parameter> getParameterList() {
      return allOf(DummyParam)                            // advertise the list of parameters
    }
  }
}