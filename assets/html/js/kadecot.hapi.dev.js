/*
 * 
 * デバイスの返答関連。kadecot.hapi.netから呼ばれることが多い。
 */

kHAPI.dev = {
  init: function() {
    this.setDevicesList();
  },
  devices: []

  ,
  findDeviceByNickname: function(nn) {
    if (typeof nn !== 'string') return;
    for (var di = 0; di < this.devices.length; ++di) {
      if (this.devices[di].nickname === nn) { return this.devices[di]; }
    }
    return;
  }

  ,
  findAssignableDevices: function(protocol, deviceType) {
    var ret = [];
    for (var di = 0; di < this.devices.length; ++di) {
      var d = this.devices[di];
      if (d.protocol === protocol && d.deviceType === deviceType && d.active)
        ret.push(d);
    }
    return ret;
  }

  ,
  setDevicesList: function(newdev) {
    if (newdev === undefined) newdev = [];
    this.devices = newdev.concat(this.emulation_devices);
    this.sortDevices();
  }
  // returns true if new device is added.
  // false if the adding device already exists
  ,
  addDevice: function(newdev) {
    // check nickname duplication
    for (var di = 0; di < this.devices.length; ++di) {
      if (this.devices[di].nickname === newdev.nickname) {
        this.devices[di] = newdev;
        return false;
      }
    }
    this.devices.unshift(newdev);
    this.sortDevices();
    return true;
  },
  removeDevice: function(nickname) {
    for (var di = 0; di < this.devices.length; ++di) {
      if (this.devices[di].nickname === nickname) {
        this.devices = this.devices.slice(0, di).concat(
                this.devices.slice(di + 1));
        return;
      }
    }
    return;
  },
  changeNickname: function(oldnickname, newnickname) {
    for (var di = 0; di < this.devices.length; ++di) {
      if (this.devices[di].nickname === oldnickname) {
        this.devices[di].nickname = newnickname;
        return true;
      }
    }
    return false;
  },
  sortDevices: function() {
    // Sorter function.
    // Device list order :
    // 1. ECHONET Lite devices
    // 2. ECHONTE Lite sensors
    // 3. Emulation devices
    var r1 = [], r2 = [], r3 = [];
    for (var di = 0; di < this.devices.length; ++di) {
      var d = this.devices[di];
      var t = null;
      if (d.isEmulation === true)
        t = r3;
      else if (d.protocol === 'ECHONET Lite') {
        if (typeof d.iRemoconDevice !== 'string') {
          // not iRemocon-connected device
          if (d.deviceType.substring(0, 4) === '0x00') // sensors
            t = r2;
          else
            t = r1;
        }
      }
      if (t === null) continue;
      t.push(d);
    }
    this.devices = r1.concat(r2).concat(r3);
  }

  ,
  'emulation_devices': [{
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": true,
    "deviceName": "HomeAirConditioner",
    "isLoggable": true,
    "nickname": "HomeAirConditioner_Emu",
    "deviceType": "0x0130",
    "access": {
      "0x80": [0x31] // 0x30:Power on, 0x31: off
      ,
      "0xb0": [0x42] // 0x41:Auto, 0x42:Cool, 0x43:Heat, 0x44:Dry, 0x45:Wind,
                      // 0x46:Others
      ,
      "0xb3": [21]
    // 21 degree
    }
  }, {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": true,
    "deviceName": "WaterHeater",
    "isLoggable": true,
    "nickname": "WaterHeater_Emu",
    "deviceType": "0x026b",
    "access": {
      "0x80": [0x30],
      "0xb0": [0x41],
      "0xd1": [40],
      "0xd3": [38],
      "0xe3": [0x41],
      "0xe4": [0x41],
      "0xe6": [0x41]
    }
  }
  // 分電盤
  , {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": true,
    "deviceName": "PowerDistributionBoardMetering",
    "isLoggable": false,
    "nickname": "PowerDistributionBoardMetering_Emu",
    "deviceType": "0x0287",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xc6": [0, 0, 0, 205] // MeasuredInstantaneoUsAmountOfElectricEnergy
      ,
      "0xc2": [3] // UnitForCumulativeAmountsOfElectricEnergy (0x00: 1kWh 0x01:
                  // 0.1kWh 0x02: 0.01kWh 0x03: 0.001kWh�iInitial value�j 0x04:
                  // 0.0001kWh 0x0A: 10kWh 0x0B: 100kWh 0x0C: 1000kWh 0x0D:
                  // 10000kWh)
      ,
      "0xd0": [0, 8, 159, 212, 0, 16, 0, 0] // ch1
      ,
      "0xd1": [0, 0, 214, 142, 0, 0, 0, 0] // ch2
      ,
      "0xd2": [0, 0, 214, 142, 0, 0, 0, 0] // ch3
      ,
      "0xd3": [0, 1, 53, 14, 0, 0, 0, 0] // ch4
      ,
      "0xd4": [0, 7, 18, 48, 0, 0, 0, 0] // ch5
      ,
      "0xd5": [0, 8, 102, 252, 0, 0, 0, 1] // ch6
      ,
      "0xd6": [0, 1, 43, 154, 0, 0, 0, 0]
    // ch7
    }
  }, {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOGeneralILighting",
    "isLoggable": false,
    "nickname": "GeneralLighting_Emu",
    "deviceType": "0x0290",
    "access": {
      "0x80": [0x30]
    // 0x30:On , 0x31:Off
    }
  }, {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOBlind",
    "isLoggable": false,
    "nickname": "Blind_Emu",
    "deviceType": "0x0260",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xe0": [0x41]
    // 0x41:Open , 0x42:Close
    }
  }, {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOCurtain",
    "isLoggable": false,
    "nickname": "Curtain_Emu",
    "deviceType": "0x0262",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xe0": [0x41]
    // 0x41:Open , 0x42:Close
    }
  }

  , {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOController",
    "isLoggable": false,
    "nickname": "Controller_Emu",
    "deviceType": "0x05ff",
    "access": {
      "0x80": [0x30]
    // 0x30:On , 0x31:Off
    }
  }
  // ,{"active":true,"protocol":"ECHONET Lite",
  // isEmulation:true,"isRemote":false,"deviceName":"ECHOAirSpeedSensor","isLoggable":false,"nickname":"AirSpeedSensor_Emu","deviceType":"0x001f"}
  , {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOHumiditySensor",
    "isLoggable": false,
    "nickname": "HumiditySensor_Emu",
    "deviceType": "0x0012",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xe0": [68]
    // percent
    }
  }, {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHORainSensor",
    "isLoggable": false,
    "nickname": "RainSensor_Emu",
    "deviceType": "0x0013",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xb1": [0x42]
    /* 0x41:raining , 0x42:not raining */
    }
  }
  // ,{"active":true,"protocol":"ECHONET Lite",
  // isEmulation:true,"isRemote":false,"deviceName":"ECHOSnowSensor","isLoggable":false,"nickname":"SnowSensor_Emu","deviceType":"0x002c"}
  , {
    "active": true,
    "protocol": "ECHONET Lite",
    isEmulation: true,
    "isRemote": false,
    "deviceName": "ECHOTemperatureSensor",
    "isLoggable": false,
    "nickname": "TemperatureSensor_Emu",
    "deviceType": "0x0011",
    "access": {
      "0x80": [0x30] // 0x30:On , 0x31:Off
      ,
      "0xe0": [0, 160]
    // 16 degree
    }
  }]
};
