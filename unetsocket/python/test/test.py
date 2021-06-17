import os
import sys
sys.path.insert(0, os.path.abspath("."))
import time
import unittest
from unetpy import *
from threading import Thread

class UnetTest(unittest.TestCase):

  def test_socket(self):
    """Test unet socket
  """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    self.assertIsInstance(usock, UnetSocket)
    self.assertEqual(usock.getLocalAddress(), 232)
    self.assertEqual(usock.host("A"), 232)
    self.assertEqual(usock.host("B"), 31)
    shell = usock.agentForService(Services.SHELL)
    self.assertIsInstance(shell, AgentID)
    self.assertEqual(shell.language, "Groovy")
    self.assertFalse(usock.isClosed())
    usock.close()
    self.assertTrue(usock.isClosed())

  def test_gateway(self):
    """Test gateway
    """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    gw = usock.getGateway()
    self.assertIsInstance(gw, Gateway)
    shell = gw.agentForService(Services.SHELL)
    self.assertIsInstance(shell, AgentID)
    self.assertEqual(shell.language, "Groovy")
    usock.close()

  def test_agents(self):
    """Test agents
    """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    node = usock.agent('node')
    self.assertIsInstance(node, AgentID)
    self.assertEqual(node.address, 232)
    self.assertEqual(node.nodeName, "A")
    phy = usock.agentForService(Services.PHYSICAL)
    self.assertIsInstance(phy, AgentID)
    self.assertEqual(phy.name, "phy")
    self.assertTrue(phy.MTU > 0)
    usock.close()

  def test_bind_and_connect(self):
    """Test bind and connect
    """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    self.assertEqual(usock.getLocalProtocol(), -1)
    self.assertFalse(usock.isBound())
    usock.bind(42)
    self.assertTrue(usock.isBound())
    self.assertEqual(usock.getLocalProtocol(), 42)
    usock.unbind()
    self.assertFalse(usock.isBound())
    self.assertEqual(usock.getLocalProtocol(), -1)
    self.assertEqual(usock.getRemoteAddress(), -1)
    self.assertEqual(usock.getRemoteProtocol(), 0)
    self.assertFalse(usock.isConnected())
    usock.connect(31, 0)
    self.assertEqual(usock.getRemoteAddress(), 31)
    self.assertEqual(usock.getRemoteProtocol(), 0)
    self.assertTrue(usock.isConnected())
    usock.disconnect()
    self.assertFalse(usock.isConnected())
    self.assertEqual(usock.getRemoteAddress(), -1)
    self.assertEqual(usock.getRemoteProtocol(), 0)
    usock.close()

  def test_timeouts(self):
    """Test timeout
    """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    usock.bind(0)
    self.assertEqual(usock.getTimeout(), -1)
    usock.setTimeout(1000)
    self.assertEqual(usock.getTimeout(), 1000)
    t1 = time.time() * 1000
    self.assertEqual(usock.receive(), None)
    dt = (time.time() * 1000) - t1
    self.assertTrue(dt > 1000)
    usock.setTimeout(0)
    self.assertEqual(usock.getTimeout(), 0)
    t1 = time.time() * 1000
    self.assertEqual(usock.receive(), None)
    dt = (time.time() * 1000) - t1
    self.assertTrue(dt < 500)
    usock.close()

  def test_cancel(self):
    """Test cancel
    """
    print(self.shortDescription())
    usock = UnetSocket('localhost', 1101)
    self.assertTrue(usock.bind(0))
    self.assertEqual(usock.getTimeout(), -1)
    t1 = time.time() * 1000
    Thread(target=cancel_task, args=[usock]).start()
    self.assertEqual(usock.receive(), None)
    dt = (time.time() * 1000) - t1
    self.assertTrue(dt > 1500)
    usock.close()

  def test_communication(self):
    """Test communication
    """
    print(self.shortDescription())
    usock1 = UnetSocket('localhost', 1101)
    usock2 = UnetSocket('localhost', 1102)
    self.assertTrue(usock2.bind(Protocol.USER))
    usock2.setTimeout(1000)
    self.assertFalse(usock1.send([1,2,3]))
    self.assertTrue(usock1.send([1,2,3], 31))
    self.assertEqual(usock2.receive(), None)
    self.assertTrue(usock1.send([1,2,3], 31, Protocol.USER))
    ntf = usock2.receive()
    self.assertIsInstance(ntf, DatagramNtf)
    self.assertEqual(ntf.data, [1,2,3])
    usock1.connect(31, Protocol.USER)
    self.assertTrue(usock1.send([1,2,3]))
    ntf = usock2.receive()
    self.assertIsInstance(ntf, DatagramNtf)
    self.assertEqual(ntf.data, [1,2,3])
    self.assertTrue(usock1.send([1,2,3], 31, 0))
    self.assertEqual(usock2.receive(), None)
    self.assertTrue(usock1.send([1,2,3], 27, Protocol.USER))
    self.assertEqual(usock2.receive(), None)
    self.assertTrue(usock1.send([1,2,3]))
    ntf = usock2.receive()
    self.assertIsInstance(ntf, DatagramNtf)
    self.assertEqual(ntf.data, [1,2,3])
    usock1.disconnect()
    self.assertFalse(usock1.send([1,2,3]))
    self.assertTrue(usock1.send([1,2,3], 31, Protocol.USER))
    ntf = usock2.receive()
    self.assertIsInstance(ntf, DatagramNtf)
    self.assertEqual(ntf.data, [1,2,3])
    usock1.close()
    usock2.close()

if __name__ == '__main__':

  def cancel_task(usock):
    time.sleep(2)
    usock.cancel()

  # start simulator
  print("Starting 2-node simulation...")
  os.system('bash test/sim.sh start')
  time.sleep(5)

  # tests
  print("Starting tests...")
  suite = unittest.TestLoader().loadTestsFromTestCase(UnetTest)
  test_result = unittest.TextTestRunner(verbosity=1).run(suite)
  failures = len(test_result.errors) + len(test_result.failures)

  # stop simulator
  print("Stopping 2-node simulation...")
  p = os.system('bash test/sim.sh stop')
  time.sleep(1)
  if p != 0:
    print("Could not stop!")