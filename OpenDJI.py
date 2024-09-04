import socket
from threading import Thread, Lock, Event
import queue
import time
import re

import av
import av.codec

class EventListener:
    def onValue(self, value):
        raise NotImplementedError("onValue not implemented")


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

        # Set background threads
        self._background_frames = BackgroundVideoCodec(self._socket_video)
        self._background_control_messages = BackgroundCommandsQueue(self._socket_control)
        self._background_query_messages = BackgroundCommandListener(self._socket_query)

    

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

        self._background_frames.stop()
        self._background_control_messages.stop()
        self._background_query_messages.stop()



    ###### Video methods ######

    def getFrame(self):
        """
        Retrive the latest frame available, or None if no frame available.
        """
        return self._background_frames.read()


    def frameListener(self, eventHandler : EventListener):
        """
        Set frame listener - an EventListener class that will be called on
        every new frame.

        Args:
            eventHandler (EventListener): a class that defines what to do when
                the drone receive new frame.
        """
        self._background_frames.registerListener(eventHandler)


    def removeFrameListener(self):
        """
        Remove the frame listener (if was set) by frameListener(listener) method.
        """
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


    def move(self, rcw : float, du : float, lr : float, bf : float, get_result: bool = False) -> str | None:
        """
        Set drone movements forces - parameters equal to control stick movement.
        All values are real numbers between -1.0 to 1.0, where 0.0 is no movement.

        Args:
            rcw (float): rotate clock wise (1.0), or anti clockwise (-1.0).
            du (float): move downward (-1.0) or upward (1.0).
            lr (float): move to the left (-1.0) or to the right (1.0).
            bf (float): move backward (-1.0) or forward (1.0).
            get_result (bool): to flag if to wait for response from the server.
        
        Return:
            Message from server (str) if get_result is true, else None.
        """
        def clip1(value):
            return min(1.0, max(-1.0, value))
        
        # Make sure the values are between -1.0 and 1.0
        rcw = clip1(rcw)
        du = clip1(du)
        lr = clip1(lr)
        bf = clip1(bf)

        # Send the command
        command = f'rc {rcw:.4f} {du:.2f} {lr:.2f} {bf:.2f}'
        self.send_command(self._socket_control, command)

        # Return result:
        if get_result:
            return self._background_control_messages.read()
        else:
            self._background_control_messages.disposeNext()


    def enableControl(self, get_result: bool = False) -> str | None:
        """
        Enable the control. This command is crucial before making movements,
        as this command removes the control from the remote controller and give
        control over the drone to the application.

        Args:
            get_result (bool): to flag if to wait for response from the server.
        
        Return:
            Message from server (str) if get_result is true, else None.
        """
        self.send_command(self._socket_control, "enable")

        # Return result:
        if get_result:
            return self._background_control_messages.read()
        else:
            self._background_control_messages.disposeNext()


    def disableControl(self, get_result: bool = False) -> str | None:
        """
        Disable the control. This command is crucial after controlling the
        drone, as this command removes the control from the program, and
        give it back to the remote controller.

        Args:
            get_result (bool): to flag if to wait for response from the server.
        
        Return:
            Message from server (str) if get_result is true, else None.
        """
        self.send_command(self._socket_control, "disable")

        # Return result:
        if get_result:
            return self._background_control_messages.read()
        else:
            self._background_control_messages.disposeNext()


    def takeoff(self, get_result: bool = False) -> str | None:
        """
        Takeoff the drone.

        Args:
            get_result (bool): to flag if to wait for response from the server.
        
        Return:
            Message from server (str) if get_result is true, else None.
        """
        self.send_command(self._socket_control, "takeoff")

        # Return result:
        if get_result:
            return self._background_control_messages.read()
        else:
            self._background_control_messages.disposeNext()


    def land(self, get_result: bool = False) -> str | None:
        """
        land the drone.

        Args:
            get_result (bool): to flag if to wait for response from the server.
        
        Return:
            Message from server (str) if get_result is true, else None.
        """
        self.send_command(self._socket_control, "land")

        # Return result:
        if get_result:
            return self._background_control_messages.read()
        else:
            self._background_control_messages.disposeNext()

    

    ###### Key-Value methods ######

    def getValue(self, module, key):
        """
        """
        return self._background_query_messages.readOnce(
            f"{module} {key}",
            f"get {module} {key}"
        )


    def listen(self, module, key, eventHandler : EventListener):
        """
        """
        self._background_query_messages.setListener(
            f"{module} {key}",
            eventHandler
        )
        self._background_query_messages.send_command(
            f"listen {module} {key}"
        )
    

    def unlisten(self, module, key):
        """
        """
        self._background_query_messages.removeListener(
            f"{module} {key}",
        )
        return self._background_query_messages.readOnce(
            f"{module} {key}",
            f"unlisten {module} {key}"
        )


    def setValue(self, module, key, value):
        """
        """
        return self._background_query_messages.readOnce(
            f"{module} {key}",
            f"set {module} {key} {value}"
        )


    def action(self, module, key, value = None):
        """
        """
        if value is None:
            return self._background_query_messages.readOnce(
                f"{module} {key}",
                f"action {module} {key}"
            )
        
        else:
            return self._background_query_messages.readOnce(
                f"{module} {key}",
                f"action {module} {key} {value}"
            )


    def help(self, module = None, key = None):
        """
        """
        if module is None:
            return self._background_query_messages.readUnbound(
                "help"
            )
        
        if key is None:
            return self._background_query_messages.readUnbound(
                f"help {module}"
            )
        
        else:
            return self._background_query_messages.readUnbound(
                f"help {module} {key}"
            )


    def getModules(self):
        return self.help()


    def getModuleKeys(self, module):
        return self.help(module)


    def getKeyInfo(self, module, key):
        return self.help(module, key)




class BackgroundCommandListener:
    """
    """

    def __init__(self, sock : socket.socket):
        """
        Initiate background messages receiver from a command manager.

        Args:
            sock (socket.socket): socket receiving the messages from.
        """
        # Internal variables
        self._send_lock = Lock()
        self._sock = sock
        self._live = True

        self._listeners = {}
        self._listeners_lock = Lock()

        self._listeners_onces_event = {}
        self._listeners_onces_result = {}
        self._listeners_onces_lock = Lock()

        self._unbound_messages = queue.Queue()
        self._message = ""

        # Starting the background thread
        self._thread = Thread(target = self.__ReadMessages__)
        self._thread.daemon = True
        self._thread.start()



    def __ReadMessages__(self):
        """
        Reads messages in the background
        """
        
        # Iterate while flag is on.
        while self._live:

            # Read data, and close thread if socket is down.
            try:
                data = self._sock.recv(1 << 20) # 1MB
                if len(data) == 0:
                    break
            except ConnectionAbortedError:
                break

            # Add the data to the total message,
            #  meging messages that araived splited.
            self._message += data.decode("utf-8")

            # Add all available complete messages to the queue
            messages_list = self._message.split("\r\n")
            
            # Add the remaining messages to be read.
            for message in messages_list[:-1]:  # Without the last.

                # If the message starts with "{", it is probably help message,
                # and if it has less then two spaces, no key can be extracted,
                # in both cases this message is more likly to be general
                if message.startswith("{") or message.count(" ") < 2:
                    self._unbound_messages.put(message)
                    continue

                message_parts = message.split(" ", 2)
                unique_key = message_parts[0] + " " + message_parts[1]
                message_trimed = message_parts[2]

                with self._listeners_onces_lock:
                    if unique_key in self._listeners_onces_event:
                        self._listeners_onces_result[unique_key] = message_trimed
                        self._listeners_onces_event[unique_key].set()
                        del self._listeners_onces_event[unique_key]
                        continue

                with self._listeners_lock:
                    if unique_key in self._listeners:
                        listener : EventListener = self._listeners[unique_key]
                        listener.onValue(message_trimed)
                        continue
                
                self._unbound_messages.put(message)

            # The last message didn't end with '\r\n',
            # and if was, then message_list[-1] = "".
            self._message = messages_list[-1]
    

    def send_command(self, command : str) -> None:
        """
        Sends a command over the socket, with 'TELNET' like protocol.

        Args:
            command (str): string to send.
        """
        with self._send_lock:
            self._sock.send(bytes(command + '\r\n', 'utf-8'))


    def readOnce(self, unique_key, command) -> str:

        event = Event()

        with self._listeners_onces_lock:
            if unique_key in self._listeners_onces_event:
                event = self._listeners_onces_event[unique_key]
            else:
                self._listeners_onces_event[unique_key] = event
                self.send_command(command)

        event.wait()
        return self._listeners_onces_result[unique_key]
    

    def readUnbound(self, command : str) -> str:

        self.send_command(command)
        return self._unbound_messages.get()
    

    def setListener(self, unique_key, listener : EventListener):

        with self._listeners_lock:
            self._listeners[unique_key] = listener

    
    def removeListener(self, unique_key):

        with self._listeners_lock:
            if unique_key in self._listeners:
                del self._listeners[unique_key]


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









class BackgroundCommandsQueue:
    """
    Reading the return message from command server, in the background.
    Helps disposing messages and make the messages order synchronized.

    Note: this class used with the control server, which does not provide
     clear message about which command the message is associated to,
     and that may lead to misleading messages sometimes.

    Note: this class implementation does not guarante message order on multi
     threaded program. e.g. if read() and disposeNext() were called from two
     different threades, even with clear order, it is not guaranteed which
     message will be read and which disposed, as I want this class
     implementation to be as simple as posible.
     
    Hint: if one would like to implement so, you would have to use another lock,
     and to add another queue, request_orders, so the order is preserved,
     and the lock will guarante that reading from the messages queue is
     synchronized with the requests queue.
    """

    def __init__(self, sock : socket.socket):
        """
        Initiate background messages receiver from a command manager.

        Args:
            sock (socket.socket): socket receiving the messages from.
        """
        # Internal variables
        self._sock = sock
        self._queue = queue.Queue()
        self._live = True
        self._message = ""
        self._dispose = 0
        self._dispose_lock = Lock()

        # Starting the background thread
        self._thread = Thread(target = self.__ReadMessages__)
        self._thread.daemon = True
        self._thread.start()


    def __ReadMessages__(self):
        """
        Reads messages in the background
        """
        
        # Iterate while flag is on.
        while self._live:

            # Read data, and close thread if socket is down.
            try:
                data = self._sock.recv(1 << 20) # 1MB
                if len(data) == 0:
                    break
            except ConnectionAbortedError:
                break

            # Add the data to the total message,
            #  meging messages that araived splited.
            self._message += data.decode("utf-8")

            # Add all available complete messages to the queue
            messages_list = self._message.split("\r\n")

            # Remove message marked to despose:
            while len(messages_list) > 1 and self._dispose > 0:
                messages_list.pop(0)
                with self._dispose_lock:
                    self._dispose -= 1
            
            # Add the remaining messages to be read.
            for message in messages_list[:-1]:  # Without the last.
                self._queue.put(message)

            # The last message didn't end with '\r\n',
            # and if was, then message_list[-1] = "".
            self._message = messages_list[-1]
        

    def read(self, block: bool = True, timeout: float | None = None) -> str | None:
        """
        Try to read a message from the server, with blocking mechanism,
        and timeout option.

        Args:
            block (bool): True to block, false to non-block.
            timeout (float | None): set timeout to wait for message,
                or None to wait indefinitely.

        Return:
            string of the last message, if available, or None if no message.
        """
        try:
            return self._queue.get(block)
        except queue.Empty:
            self.disposeNext()
        return None


    def disposeNext(self):
        """ Set to dispose (ignore) the next received message. """
        with self._dispose_lock:
            self._dispose += 1


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
            try:
                data = self._sock.recv(1 << 20) # 1MB
                if len(data) == 0:
                    break
            except ConnectionAbortedError:
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

