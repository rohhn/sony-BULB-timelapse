import time
import pysony


class NoCameraFoundException(Exception):
    pass


class CameraError(Exception):

    def __init__(self, msg="An error occurred."):
        self.msg = msg


def echo_test():
    return "Hello World!"


class Camera:

    def __init__(self):
        self.avail_api_list = None
        self.camera = None
        self.is_connected = False

    @staticmethod
    def _search():

        s = pysony.ControlPoint()
        cameras = s.discover(1)

        if not cameras:
            raise NoCameraFoundException

        return cameras[0]

    def initiate(self):
        """
        Shorthand for connect and setup
        :return:
        """

        self.connect()
        self.setup()

    def connect(self):
        """
        Search for available cameras and initiate the connection
        :return:
        """

        camera_url = self._search()
        self.camera = pysony.SonyAPI(QX_ADDR=camera_url)

        self.avail_api_list = self.camera.getAvailableApiList()

        if not isinstance(self.avail_api_list, dict):
            raise NoCameraFoundException

        if "startRecMode" in self.avail_api_list['result'][0]:
            self.camera.startRecMode()
            time.sleep(5)

        self.is_connected = True

    def setup(self):

        """
        Change exposure mode to BULB
        Check autofocus mode and set focus
        Turn off Auto ISO
        :return:
        """

        if self.camera is None:
            raise NoCameraFoundException

        shutter_speed = self.camera.getAvailableShutterSpeed()
        if not isinstance(shutter_speed, dict) or "error" in shutter_speed:
            raise CameraError("Failed to fetch information from camera")

        if "BULB" not in shutter_speed['result'][1]:
            raise CameraError("BULB Mode not supported")

        if shutter_speed['result'][0] != "BULB":
            self.camera.setShutterSpeed(["BULB"])

        # focus_mode = self.camera.getAvailableFocusMode()
        # if not isinstance(focus_mode, dict) or "error" in focus_mode:
        #     raise CameraError("Failed to fetch information from camera")
        #
        # if "MF" not in focus_mode['result'][1]:
        #     raise CameraError("Unable to set Manual Focus")
        #
        # if focus_mode['result'][0] != "MF":
        #     self.camera.setFocusMode(["MF"])
        #
        # iso_rate = self.camera.getAvailableIsoSpeedRate()
        # if not isinstance(focus_mode, dict) or "error" in focus_mode:
        #     raise CameraError("Failed to fetch information from camera")
        #
        # if iso_rate['result'][0] == "AUTO":
        #     raise CameraError("Auto ISO is not supported in BULB mode")

        return

    def terminate(self):
        if self.is_connected:
            if "stopRecMode" in self.avail_api_list['result'][0]:
                self.camera.stopRecMode()

    def start_bulb_shooting(self):
        self.camera.startBulbShooting()

    def stop_bulb_shooting(self):
        self.camera.stopBulbShooting()

    def startstop_bulb(self, shutter_speed):
        self.start_bulb_shooting()
        time.sleep(shutter_speed)
        self.stop_bulb_shooting()
