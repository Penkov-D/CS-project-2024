import socket
from threading import Thread

import av
import av.codec

class EventListener:

    def onValue(self, value):
        raise NotImplementedError("onValue not implemented")

    def onError(self, ):
        raise NotImplementedError("onError not implemented")


class OpenDJI:

    # Available Modules
    MODULE_GIMBAL = "Gimbal"
    MODULE_REMOTECONTROLLER = "RemoteController"
    MODULE_FLIGHTCONTROLLER = "FlightController"
    MODULE_BATTERY = "Battery"
    MODULE_AIRLINK = "AirLink"
    MODULE_PRODUCT = "Product"
    MODULE_CAMERA = "Camera"

    # Pre-defined ports for communication channels.
    PORT_VIDEO   = 9999
    PORT_CONTROL = 9998
    PORT_QUERY   = 9997

    def __init__(self, host):

        self.host_address = host

        # Establish network connection
        self._socket_video = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket_control = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._socket_query = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        try:
            self._socket_video.connect((self.host_address, self.PORT_VIDEO))
            self._socket_control.connect((self.host_address, self.PORT_CONTROL))
            self._socket_query.connect((self.host_address, self.PORT_QUERY))
            
        except Exception as e:
            self._socket_video.close()
            self._socket_control.close()
            self._socket_query.close()
            raise

        # All network is set.

        # Set video thread
        self._background_frames = BackgroundVideoCodec(self._socket_video)


    

    ###### Object handling methods ######

    def __enter__(self):
        return self


    def __exit__(self, exc_type, exc_value, traceback):
        self.close()


    def close(self):
        """
        Clean after the object, closes all the communication and threads.
        """
        self._socket_video.close()
        self._socket_control.close()
        self._socket_query.close()

        self._background_frames.stop(0)


    ###### Video methods ######

    def getFrame(self):
        return self._background_frames.read()

    def frameListener(self, eventHandler : EventListener):
        self._background_frames.registerListener(eventHandler)

    def removeFrameListener(self):
        self._background_frames.unregisterListener()



    ###### Control methods ######

    def send_command(self, sock : socket.socket, command : str) -> None:
        """
        Sends a command over the socket, with 'TELNET' like protocol.

        Args:
            sock (socket.socket): socket to communicate over.
            command (str): string to send.
        """
        sock.send(bytes(command + '\r\n', 'utf-8'))

    
    def receive_message(self, sock : socket.socket) -> str:
        """
        Receives the next message from the server.
        """
    

    def move(self, rcw : float, dw : float, lr : float, bf : float, ):
        """
        Move the drone
        """
        pass

    def enableControl(self):
        pass

    def disableControl(self):
        pass

    def takeoff(self):
        pass

    def land(self):
        pass

    
    ###### Key-Value methods ######

    def getValue(self, module, key):
        pass

    def getLastValue(self, module, key):
        pass

    def getValueAsync(self, module, key, eventHandler : EventListener):
        pass

    def listen(self, module, key, eventHandler : EventListener):
        pass
    
    def unlisten(self, module, key):
        pass

    def setValue(self, module, key, value):
        pass

    def action(self, module, key, value = None):
        pass


    def help(self, module = None, key = None):
        pass

    def getModules(self):
        pass

    def getModuleKeys(self, module):
        pass

    def getKeyInfo(self, module, key):
        pass



class BackgroundVideoCodec:
    """
    Capturing the frames in the background,
    so the frame processing doesn't lag the program,
    and the most recent frame will return instantly.

    Currently retrive the frames only in H264 format.
    The drone also supports H265, but doesn't use it for now.
    If there are errors, feel free to change the codec.

    Used internally.
    """

    def __init__(self, sock : socket.socket):
        """
        Initiate background video codec, and start it right away.
        Expecting a open and connected socket to retrive the frames from.

        Args:
            sock (socket.socket): socket receiving the video from.
        """
        # Internal variables
        self._sock = sock
        self._frame = None
        self._codec = av.codec.context.CodecContext.create('h264', 'r')
        self._live = True
        self._listener = None

        # Starting the background thread
        self._thread = Thread(target = self.__ReadFrames__)
        self._thread.daemon = True
        self._thread.start()


    def __ReadFrames__(self):
        """
        Reads frame in the background
        """
        
        # Iterate while flag is on.
        while self._live:

            # Read data, and close thread if socket is down.
            data = self._sock.recv(1 << 20) # 1MB
            if len(data) == 0:
                break

            # Iterate thru the packets from the data,
            # and decode the frames from the packets.
            for packet in self._codec.parse(data):
                for frame in self._codec.decode(packet):

                    self._frame = frame.to_ndarray(format = 'bgr24')

                    # Call the listener with the new frame
                    #  save listener in new variable to avoid
                    #  multi-threading bugs.
                    listener : EventListener = self._listener

                    if listener:
                        listener.onValue(self._frame)

        # Set frame to None, if connection / thread interuptted.    
        self._frame = None


    def read(self):
        """ Get the last available frame from this video stream. """
        return self._frame


    def stop(self, timeout : float | None = None):
        """
        Stop the thread. (Also closes the socket)

        Args:
            timeout (float | None): timeout for the operation in seconds,
                or None to wait indefenetly.
        """
        self._live = False
        self._sock.close()
        self._thread.join(timeout)


    def registerListener(self, listener : EventListener):
        """ Sets frame listener """
        self._listener = listener


    def unregisterListener(self):
        """ Remove frame listener """
        self._listener = None

