# QRLink
## QR-code based Unet optical communication link

This agent provides a QR-code based optical communication link between two Unet nodes. The link is implemented by displaying QR codes in a window and decoding the QR codes using a camera (laptop camera / webcam). This is not production-ready code, but a demonstration of how a simple optical link can be implemented with UnetStack.

### Dependencies

- [UnetStack](www.unetstack.net) (`unet-framework-3.x.x.jar`)
- [JavaCV](https://github.com/bytedeco/javacv) (maven: `org.bytedeco:javacv-platform:1.5.5`)
- [ZXing](https://github.com/zxing/zxing) (maven: `com.google.zxing:core:3.5.0`, `com.google.zxing:javase:3.5.0`)

### Getting QRLink running with Unet simulator or Unet audio

- Put source Groovy file in `classes` folder
- Put all JavaCV and ZXing dependency jars in `jars` folder
- Load on both nodes using: `container.add 'qrlink', new QRLink()`
- Point each node's camera to the screen of the peer node
- Subscribe to notifications from `qrlink` using: `subscribe qrlink`
- Transmit datagrams to each other using: `qrlink << new DatagramReq(data: [1,2,3])`
