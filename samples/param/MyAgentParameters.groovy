///////////////////////////////////////////////////////////////////////////////
///
/// List of parameters supported by sample agent class 'MyAgent'
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.unet.Parameter

@com.google.gson.annotations.JsonAdapter(org.arl.unet.JsonTypeAdapter.class)
enum MyAgentParameters implements Parameter {
  retryCount,
  retryTimeout
}
