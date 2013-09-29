if(String.prototype.format === undefined){
  String.prototype.format = function(args){
    var replace_function;
    if(typeof args === "object"){
      replace_function = function(m,k){
        return args[k];
      };
    }else{
      replace_function = function(m,k){
        return arguments[parseInt(k)];
      };
    }
    return this.replace(/\{(\w+)\}/g,replace_function);
  };
}
// https://developer.mozilla.org/ja/docs/Web/JavaScript/Reference/Global_Objects/Function/bind
if(Function.prototype.bind === undefined){
  Function.prototype.bind = function (oThis) {
    if (typeof this !== "function") {
      // closest thing possible to the ECMAScript 5 internal IsCallable function
      throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
    }

    var aArgs = Array.prototype.slice.call(arguments, 1),
        fToBind = this,
        fNOP = function () {},
        fBound = function () {
          return fToBind.apply(this instanceof fNOP && oThis
                                 ? this
                                 : oThis,
                               aArgs.concat(Array.prototype.slice.call(arguments)));
        };

    fNOP.prototype = this.prototype;
    fBound.prototype = new fNOP();

    return fBound;
  };
}
var make_dual_graph = function(div_id,options){
  return new DualGraph(div_id,options);
};
var canvas_str = function(width,height,z_index){
  return "<canvas width='{width}' height='{height}' style='position: absolute; \
                  left: 0; top: 0; z-index: {z_index}'/>"
                           .format({width:width,height:height,z_index:z_index});
};
var option_or_default = function(option,property,default_value){
  if(option === undefined || option === null || option[property] === undefined){
    return default_value;
  }else{
    return option[property];
  }
};
var DualGraph = function(div_id,options){
  // http://stackoverflow.com/questions/3008635/html5-canvas-element-multiple-layers
  if(div_id[0] !== "#") div_id = "#" + div_id;
  this.div = $(div_id);
  this.div.empty();
  this.div.css("position","relative");

  // canvas setup.make layer.
  this.width  = this.div.width();
  this.height = this.div.height();

  this.background_layer = $(canvas_str(this.width,this.height,0)).appendTo(this.div)[0];
  this.sensor_layer     = $(canvas_str(this.width,this.height,1)).appendTo(this.div)[0];
  this.device_layer     = $(canvas_str(this.width,this.height,2)).appendTo(this.div)[0];
  this.control_layer    = $(canvas_str(this.width,this.height,3)).appendTo(this.div)[0];

  this.background_ctx = this.background_layer.getContext("2d");
  this.sensor_ctx     = this.sensor_layer.getContext("2d");
  this.device_ctx     = this.device_layer.getContext("2d");
  this.control_ctx    = this.control_layer.getContext("2d");

  this.graph_margin = 10;
  // add margin.
  this.width  -= this.graph_margin;
  this.height -= this.graph_margin;

  // various value settings.if provided by option,use it.
  this.device_name_width = option_or_default(options,"device_name_width",100);
  this.sensor_graph_height_ratio = option_or_default(options,
                                                     "sensor_graph_height_ratio",1.0/3);

  this.font_size = option_or_default(options,"font_size",this.graph_margin);
  this.background_ctx.font = this.sensor_ctx.font
                           = this.device_ctx.font
                           = this.control_ctx.font
                           =  "normal normal " + this.font_size + "px sans-serif";

  this.sensor_graph_height = Math.round(this.height * this.sensor_graph_height_ratio);
  this.device_graph_height = this.height-this.sensor_graph_height;

  // constant definition.
  this.sensor_color = option_or_default(options,"sensor_color",
                      ["rgb(192, 80, 77)","rgb(155, 187, 89)", "rgb(128, 100, 162)"]);

  this.graph_background = "blanchedalmond";
  this.axis_color       = "black";
  this.axis_width       = 1;
  this.axis_alpha       = 1;
  this.background_color = "white";
  this.text_color       = "black";
  this.text_alpha       = 1;

  this.tick_width       = 1;
  this.tick_alpha       = 0.3;

  this.device_line_alpha   = 1;
  this.device_point_radius = 4;
  this.device_point_alpha  = 1;

  this.sensor_line_width   = 2;
  this.sensor_line_alpha   = 0.7;
  this.sensor_point_radius = 3;
  this.sensor_point_alpha  = 1;

  this.current_bar_width   = 2;
  this.current_bar_alpha   = 0.5;
  this.current_bar_color   = "black";

  // callback settings.
  var when_switch_device = option_or_default(options,"when_switch_device",
                                             function(dev){console.log(dev);});
  var when_switch_sensor = option_or_default(options,"when_switch_sensor",
                                             function(sen){console.log(sen);});

  $(this.control_layer).bind("mousedown",function(e){
    var rect   = e.target.getBoundingClientRect();
    var mouseX = e.clientX - rect.left;
    var mouseY = e.clientY - rect.top;
    if(mouseX <= this.device_name_width){
      var sel = Math.floor((mouseY - this.graph_margin + this.font_size)
                           / (this.font_size*2));
      if(this.sensors !== undefined && this.sensors.length > sel){
        var new_enabled = this.sensor_enabled.slice();
        new_enabled[sel] = !new_enabled[sel];
        this.enable_sensor_graph(new_enabled);
        when_switch_sensor(this.sensors[sel]);
      }else if(this.devices !== undefined &&
               this.sensors.length + this.devices.length > sel){
        var new_enabled = this.device_enabled.slice();
        sel -= this.sensors.length;
        new_enabled[sel] = !new_enabled[sel];
        this.enable_device_graph(new_enabled);
        when_switch_device(this.devices[sel]);
      }
    }
  }.bind(this));
};

// fill background_layer with background_color.
DualGraph.prototype.draw_background = function(){
  this.background_ctx.clearRect(0,0,this.width+this.graph_margin,
                                this.height+this.graph_margin);
  this.background_ctx.fillStyle = this.graph_background;
  this.background_ctx.fillRect(this.device_name_width,this.graph_margin,
                               this.width-this.device_name_width,
                               this.height-this.graph_margin);
};

// draw axis.ticks is like this.
// var  = ["0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15",
//                "16","17","18","19","20","21","22","23","24"];
DualGraph.prototype.draw_axis = function(time_ticks,sensor_ticks){
  if(time_ticks !== undefined) this.time_ticks = time_ticks;
  if(sensor_ticks !== undefined) this.sensor_ticks = sensor_ticks;
  if(this.time_ticks === undefined || this.sensor_ticks === undefined) return;
  // write axis.
  {
    this.background_ctx.beginPath();
    this.background_ctx.strokeStyle = this.axis_color;
    this.background_ctx.lineWidth = this.axis_width;
    this.background_ctx.globalAlpha = this.axis_alpha;
    // x-axis
    this.background_ctx.moveTo(this.device_name_width,this.sensor_graph_height);
    this.background_ctx.lineTo(this.width,this.sensor_graph_height);

    // bottom-line
    this.background_ctx.moveTo(this.device_name_width,this.height);
    this.background_ctx.lineTo(this.width,this.height);
    // top-line
    this.background_ctx.moveTo(this.device_name_width,this.graph_margin);
    this.background_ctx.lineTo(this.width,this.graph_margin);
    // right-line
    this.background_ctx.moveTo(this.width,this.graph_margin);
    this.background_ctx.lineTo(this.width,this.height);

    // y-axis
    this.background_ctx.moveTo(this.device_name_width,this.graph_margin);
    this.background_ctx.lineTo(this.device_name_width,this.height);

    this.background_ctx.stroke();
    this.background_ctx.closePath();
  }

  // write y tick.
  {
    this.background_ctx.textAlign = "right";
    this.background_ctx.textBaseline = "middle";
    var margin = Math.round((this.sensor_graph_height-this.graph_margin)
                            / (this.sensor_ticks.length-1));

    for(var i=0;i<this.sensor_ticks.length;i++){
      var y = this.sensor_graph_height - margin * i;
      var x = this.device_name_width;

      this.background_ctx.beginPath();
      this.background_ctx.lineWidth = this.tick_width;
      this.background_ctx.globalAlpha = this.tick_alpha;
      this.background_ctx.moveTo(x,y);
      this.background_ctx.lineTo(this.width,y);
      this.background_ctx.stroke();
      this.background_ctx.closePath();

      this.background_ctx.globalAlpha = this.text_alpha;
      this.background_ctx.fillStyle = this.text_color;
      this.background_ctx.fillText(this.sensor_ticks[i],x-3,y);
    }
  }

  // write x tick.
  {
    this.background_ctx.textAlign = "right";
    this.background_ctx.textBaseline = "top";
    // var margin = Math.round((this.width - this.device_name_width)
    //                         / (this.time_ticks.length-1));
    var margin = ((this.width - this.device_name_width)
                            / (this.time_ticks.length-1));
    for(var i=0;i<this.time_ticks.length;i++){
      var x = Math.round(this.device_name_width + margin * i);
      var y = this.sensor_graph_height;
      this.background_ctx.beginPath();
      this.background_ctx.lineWidth = this.time_width;
      this.background_ctx.globalAlpha = this.tick_alpha;
      this.background_ctx.moveTo(x,this.graph_margin);
      this.background_ctx.lineTo(x,this.height);
      this.background_ctx.stroke();
      this.background_ctx.closePath();

      this.background_ctx.globalAlpha = this.text_alpha;
      this.background_ctx.fillText(this.time_ticks[i],x,y);
    }
  }
};

// devices is array of string.
//  enabled is array of bool which means display or not.
// device_data is [[[x1,title1,message1],[x2,title2,message2]],[...]]...
DualGraph.prototype.draw_device_graph = function(devices,device_enabled,device_data){
  this.device_ctx.clearRect(0,0,this.width,this.height);
  var x_pos_index = 0;
  var title_index = 1;
  var message_index = 2;

  if(devices !== undefined) this.devices = devices;
  if(device_enabled !== undefined) this.device_enabled = device_enabled;
  if(device_data !== undefined) this.device_data = device_data;
  if(this.devices === undefined || this.device_enabled === undefined
                                || this.device_data === undefined) return;

  var number_of_enabled = this.device_enabled.filter(function(e){return e;}).length;
  var init_y = this.sensor_graph_height;
  var init_x = this.device_name_width;

  var y_margin = Math.round((this.height - init_y) / (number_of_enabled+1));
  var x_margin = Math.round((this.width - init_x));
  for(var i=0,written=0;i<this.devices.length;i++,written++){
    var nickname = this.devices[i];
    var enable = this.device_enabled[i];
    var data = this.device_data[i];
    if(!enable) {
      // devices.length is not equal to number_of_enabled.
      written--;
      continue;
    }
    var y = init_y + y_margin * (written+1);

    // draw device name;
    this.device_ctx.globalAlpha = this.text_alpha;
    this.device_ctx.fillStyle = this.text_color;
    this.device_ctx.textAlign = "right";
    this.device_ctx.textBaseline = "middle";
    this.device_ctx.fillText(nickname,init_x-3,y);

    // draw y-axis.
    this.device_ctx.beginPath();
    this.device_ctx.lineWidth = this.axis_width;
    this.device_ctx.strokeStyle = this.axis_color;
    this.device_ctx.globalAlpha = this.tick_alpha;

    this.device_ctx.moveTo(init_x,y);
    this.device_ctx.lineTo(this.width,y);
    this.device_ctx.stroke();
    this.device_ctx.closePath();

    // draw points and title (need line?)
    this.device_ctx.globalAlpha = this.device_point_alpha;
    for(var j=0;j<data.length;j++){
      var d = data[j];
      var x = Math.round(init_x + x_margin * d[x_pos_index]);
      this.device_ctx.beginPath();
      this.device_ctx.arc(x,y,this.device_point_radius,0,2*Math.PI,false);
      //TODO: make color variable.
      this.device_ctx.fillStyle = "red";
      this.device_ctx.fill();
      this.device_ctx.closePath();

      var title_bottom = y - this.device_point_radius;
      var title_top = title_bottom - this.font_size;
      var title_width = this.device_ctx.measureText(d[title_index]).width;
      this.device_ctx.fillStyle = this.background_color;
      this.device_ctx.fillRect(Math.floor(x - title_width / 2),title_top,title_width+1,this.font_size);
      this.device_ctx.fillStyle = this.text_color;
      this.device_ctx.textAlign = "center";
      this.device_ctx.textBaseline = "bottom";
      this.device_ctx.fillText(d[title_index],x,title_bottom);
    }
  }
};

// sensors = list of sensor's name.
// enabled is array of bool which means display or not.
//  data is array of array of array,like this.
//  [ data1                    , data2]
//   [[x1,y1,title1,message1],[x2,y2,title2,message2],... ]
//  [[[0.1,1,"Temp 10C",],.....,[....]
//   x-axis must be increasing order.
//  TODO, do not use many loops !!!!
DualGraph.prototype.draw_sensor_graph = function(sensors,sensor_enabled,
                                                    sensor_data){
  var x_pos_index = 0;
  var y_pos_index = 1;
  var title_index = 2;
  var message_index = 3;
  this.sensor_ctx.clearRect(0,0,this.width,this.height);

  if(sensors !== undefined)  this.sensors = sensors;
  if(sensor_enabled !== undefined) this.sensor_enabled = sensor_enabled;
  if(sensor_data !== undefined) this.sensor_data = sensor_data;
  if(this.sensors === undefined || this.sensor_enabled === undefined
                                || this.sensor_data === undefined) return;

  for(var sensor_index=0;sensor_index<this.sensors.length;sensor_index++){
    var nickname = this.sensors[sensor_index];
    var enable = this.sensor_enabled[sensor_index];
    var data = this.sensor_data[sensor_index];
    if(!enable) continue;
    // loop color.
    var color = this.sensor_color[sensor_index%this.sensor_color.length];
    this.sensor_ctx.beginPath();
    this.sensor_ctx.strokeStyle = color;
    this.sensor_ctx.lineWidth = this.sensor_line_width;
    this.sensor_ctx.globalAlpha = this.sensor_line_alpha;
    var y_margin = Math.round(this.sensor_graph_height-this.graph_margin);
    var x_margin = Math.round(this.width-this.device_name_width);
    // draw line at first.
    for(var data_index=0;data_index<data.length;data_index++){
      var d = data[data_index];
      var x = this.device_name_width +  x_margin* d[x_pos_index];
      var y = this.sensor_graph_height - y_margin * d[y_pos_index];
      if(data_index == 0){
        this.sensor_ctx.moveTo(x,y);
      }else{
        this.sensor_ctx.lineTo(x,y);
      }
    }
    this.sensor_ctx.stroke();
    this.sensor_ctx.closePath();
  }
  for(var sensor_index=0;sensor_index<this.sensors.length;sensor_index++){
    var color = this.sensor_color[sensor_index%this.sensor_color.length];
    var nickname = this.sensors[sensor_index];
    var enable = this.sensor_enabled[sensor_index];
    var data = this.sensor_data[sensor_index];
    if(!enable) continue;
    // draw point.
    for(var data_index=0;data_index<data.length;data_index++){
      var d = data[data_index];
      var x = this.device_name_width +  x_margin* d[x_pos_index];
      var y = this.sensor_graph_height - y_margin * d[y_pos_index];
      this.sensor_ctx.beginPath();
      this.sensor_ctx.arc(x,y,this.sensor_point_radius,0,2*Math.PI,false);
      this.sensor_ctx.fillStyle = color;
      this.sensor_ctx.strokeStyle = color;
      this.sensor_ctx.globalAlpha = this.sensor_point_alpha;
      this.sensor_ctx.fill();
      this.sensor_ctx.stroke();
      this.sensor_ctx.closePath();
    }
  }
  for(var sensor_index=0;sensor_index<this.sensors.length;sensor_index++){
    var color = this.sensor_color[sensor_index%this.sensor_color.length];
    var nickname = this.sensors[sensor_index];
    var enable = this.sensor_enabled[sensor_index];
    var data = this.sensor_data[sensor_index];
    if(!enable) continue;
    // draw title.
    for(var data_index=0;data_index<data.length;data_index++){
      var d = data[data_index];
      var x = this.device_name_width +  x_margin* d[x_pos_index];
      var y = this.sensor_graph_height - y_margin * d[y_pos_index];
      var title_bottom = y - this.sensor_point_radius-1;
      var title_top = title_bottom - this.font_size;
      var title_width = this.sensor_ctx.measureText(d[title_index]).width;
      this.sensor_ctx.fillStyle = this.background_color;
      this.sensor_ctx.strokeStyle = color;
      this.sensor_ctx.fillRect(Math.floor(x - title_width / 2),title_top,title_width+1,this.font_size);
      this.sensor_ctx.fillStyle = this.text_color;
      this.sensor_ctx.textAlign = "center";
      this.sensor_ctx.textBaseline = "bottom";
      this.sensor_ctx.fillText(d[title_index],x,title_bottom);
    }
  }
};
// call draw_sensor_graph at first.
DualGraph.prototype.enable_sensor_graph = function(sensor_enabled){
  if(sensor_enabled === this.sensor_enabled) return;
  this.draw_sensor_graph(undefined,sensor_enabled,undefined);
  this.draw_switch();
};
DualGraph.prototype.enable_device_graph = function(device_enabled){
  if(device_enabled === this.device_enabled) return;
  this.draw_device_graph(undefined,device_enabled,undefined);
  this.draw_switch();
};

// current is [0,1];
DualGraph.prototype.draw_current_bar = function(current,title){
  if(current !== undefined) this.current = current;
  if(title !== undefined) this.title = title;
  if(this.current === undefined) return;

  var x = this.device_name_width + this.current * (this.width - this.device_name_width);
  this.control_ctx.clearRect(this.device_name_width,0,this.width,this.height);
  // if it is not in graph,return.
  if(this.current < 0 || this.current > 1) return;

  this.control_ctx.beginPath();
  this.control_ctx.strokeStyle = this.current_bar_color;
  this.control_ctx.globalAlpha = this.current_bar_alpha;
  this.control_ctx.lineWidth   = this.current_bar_width;
  this.control_ctx.moveTo(x,this.graph_margin);
  this.control_ctx.lineTo(x,this.height);
  this.control_ctx.stroke();
  this.control_ctx.closePath();
  this.control_ctx.textAlign = "center";
  this.control_ctx.textBaseline = "bottom";
  this.control_ctx.fillStyle = this.text_color;
  this.control_ctx.fillText(this.title,x,this.graph_margin);
};

DualGraph.prototype.draw_switch = function(sensors,sensor_enabled,
                                           devices,device_enabled){
  this.control_ctx.clearRect(0,0,this.device_name_width,this.height);
  var init_y = this.graph_margin;
  var init_x = 1;
  var margin_y = 2*this.font_size;
  var block_size = this.font_size;

  for(var s=0;s<this.sensors.length;s++){
    var color = this.sensor_color[s%this.sensor_color.length];
    var x = init_x;
    var y = init_y + margin_y * s;
    // block.
    if(!this.sensor_enabled[s]){
      this.control_ctx.globalAlpha = 0.3;
    }else{
      this.control_ctx.globalAlpha = 1;
    }
    this.control_ctx.fillStyle = color;
    this.control_ctx.fillRect(x,y-block_size/2,block_size,block_size);

    this.control_ctx.textAlign = "left";
    this.control_ctx.textBaseline = "middle";
    this.control_ctx.fillStyle = this.text_color;
    this.control_ctx.fillText(this.sensors[s],x+block_size+1,y);
  }
  for(var d=0;d<this.devices.length;d++){
    var color = "red";
    var x = init_x;
    var y = init_y + margin_y * ((this.sensors.length)+d);
    if(!this.device_enabled[d]){
      this.control_ctx.globalAlpha = 0.3;
    }else{
      this.control_ctx.globalAlpha = 1;
    }
    this.control_ctx.fillStyle = color;
    this.control_ctx.fillRect(x,y-block_size/2,block_size,block_size);
    this.control_ctx.textAlign = "left";
    this.control_ctx.textBaseline = "middle";
    this.control_ctx.fillStyle = this.text_color;
    this.control_ctx.fillText(this.devices[d],x+block_size+1,y);
  }

};


// if you call this with no argument,canvas and div will be same size.
DualGraph.prototype.resize = function(width,height){
  if(width === undefined) width = this.div.width();
  if(height === undefined) height = this.div.height();

  $(this.background_layer).attr("width",width).attr("height",height);
  $(this.sensor_layer).attr("width",width).attr("height",height);
  $(this.device_layer).attr("width",width).attr("height",height);
  $(this.control_layer).attr("width",width).attr("height",height);

  this.background_ctx = this.background_layer.getContext("2d");
  this.sensor_ctx     = this.sensor_layer.getContext("2d");
  this.device_ctx     = this.device_layer.getContext("2d");
  this.control_ctx    = this.control_layer.getContext("2d");

  this.width = width;
  this.height = height;
  this.width  -= this.graph_margin;
  this.height -= this.graph_margin;
  this.sensor_graph_height = Math.round(this.height * this.sensor_graph_height_ratio);
  this.device_graph_height = this.height-this.sensor_graph_height;

  this.rewrite_all();
};
DualGraph.prototype.rewrite_all = function(){
  this.draw_background();
  this.draw_axis();
  this.draw_device_graph();
  this.draw_sensor_graph();
  this.draw_current_bar();
  this.draw_switch();
};
